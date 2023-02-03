package ic.ufal.ifdefcatcher

import utils.{CppStatsHandler, ProjectFilter}

import org.repodriller.domain.{Commit, Modification, ModificationType}
import org.repodriller.persistence.PersistenceMechanism
import org.repodriller.scm.{CommitVisitor, RepositoryFile, SCMRepository}

import java.io.{File, FileWriter}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.CollectionHasAsScala

class MyVisitor extends CommitVisitor {

  /**
   * Visit each file on the list files, and write report
   *
   * @param files  files to visit. This is a list of a tuple. For each file, the first parameter is the file on the
   *               current commit; second parameter is the old path (on the parent commit)
   * @param repo   repository information
   * @param commit current commit
   * @param writer writer to report the results
   */
  @tailrec
  private def visitFile(files: Iterable[(RepositoryFile, String)], repo: SCMRepository, commit: Commit,
                        writer: PersistenceMechanism): Unit = {
    if (files == Nil || files.isEmpty) return
    val file = files.head

    val cppStatsHandler = new CppStatsHandler(repo.getOrigin.substring(repo.getOrigin.lastIndexOf("/") + 1), commit.getHash)
    if (cppStatsHandler.prepCppstatsDir()) {

      if (cppStatsHandler.addFileProject1(file._1.getFile) && !commit.getParents.isEmpty) {
        repo.getScm.checkout(commit.getParents.get(0))

        if (cppStatsHandler.addFileProject2(new File(file._2))) {

          // if two files, run cppstats on each and get stats (maybe Romero script?)
          if (cppStatsHandler.runCppstats()) {
            val newMetrics = cppStatsHandler.getMetricsProject1
            val oldMetrics = cppStatsHandler.getMetricsProject2

            if (oldMetrics.discipline != newMetrics.discipline &&
              oldMetrics.nonDiscipline != newMetrics.nonDiscipline) {
              synchronized {
                writer.write(
                  repo.getOrigin,
                  file._2.substring(repo.getPath.length + 1),
                  file._1.getFile.getPath.substring(repo.getPath.length + 1),
                  oldMetrics.loc,
                  newMetrics.loc,
                  oldMetrics.ifDefBlocks,
                  newMetrics.ifDefBlocks,
                  oldMetrics.discipline,
                  newMetrics.discipline,
                  oldMetrics.nonDiscipline,
                  newMetrics.nonDiscipline,
                  commit.getHash,
                  commit.getAuthor.toString.replaceAll(",", ";")
                )
              }
            }
          }
        }
      }

      // clean source
      cppStatsHandler.clean()

      repo.getScm.checkout(commit.getHash)
    }

    visitFile(files.tail, repo, commit, writer)
  }

  private def getFilesToAnalyze(modifications: Iterable[Modification], files: Iterable[RepositoryFile], basePath: String)
  : List[(RepositoryFile, String)] = {
    if (modifications.isEmpty) return Nil

    val modification = modifications.head
    if (modification.getType == ModificationType.MODIFY) {
      val file = findFile(modification, files, basePath)
      if (file._1 != null) {
        return file :: getFilesToAnalyze(modifications.tail, files, basePath)
      }
    }

    getFilesToAnalyze(modifications.tail, files, basePath)
  }

  @tailrec
  private def findFile(modification: Modification, files: Iterable[RepositoryFile], basePath: String)
  : (RepositoryFile, String) = {
    if (files.isEmpty) return (null, null)

    if (files.head.getFullName.equals(basePath + File.separator + modification.getNewPath))
      (files.head, basePath + File.separator + modification.getOldPath)
    else findFile(modification, files.tail, basePath)
  }

  private def filterFiles(files: Iterable[RepositoryFile]): List[RepositoryFile] = {
    if (files.isEmpty) return Nil
    val file = files.head
    if ((file.fileNameEndsWith(".c") || file.fileNameEndsWith(".h") ||
      file.fileNameEndsWith(".C") || file.fileNameEndsWith(".H")) &&
      (ProjectFilter.isEmpty || ProjectFilter.contains(file.getFile.getName))) {
      return file :: filterFiles(files.tail)
    }
    filterFiles(files.tail)
  }

  override def process(repo: SCMRepository, commit: Commit, writer: PersistenceMechanism): Unit = {
    try {
      repo.getScm.checkout(commit.getHash)
      val files = filterFiles(repo.getScm.files().asScala)
      if (files != Nil && files.nonEmpty) {
        this.visitFile(
          getFilesToAnalyze(commit.getModifications.asScala, files, repo.getPath),
          repo,
          commit,
          writer)
      }

    } catch {
      case e: Exception => println("ERROR: " + e.getMessage())
    } finally {
      synchronized {
        ProjectFilter.countCommits += 1
        println("PROGRESS " + ProjectFilter.countCommits + "/" + ProjectFilter.totalCommits)
        val out_writer = new FileWriter("count-steps.txt")
        try {
          out_writer.write(repo.getOrigin + " " + ProjectFilter.countCommits + "/" + ProjectFilter.totalCommits)
        } catch {
          case _: Exception =>
        } finally {
          out_writer.close()
        }
      }
      repo.getScm.reset()
    }
  }
}
