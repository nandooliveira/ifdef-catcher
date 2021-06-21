package ic.ufal.ifdefcatcher

import utils.{Project, ProjectFilter}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.commons.io.FileUtils
import org.repodriller.filter.range.Commits
import org.repodriller.persistence.csv.CSVFile
import org.repodriller.scm.GitRemoteRepository
import org.repodriller.{RepositoryMining, Study}

import java.io.{File, FileWriter}
import scala.annotation.tailrec
import scala.io.Source
import scala.util.{Try, Using}

class MyStudy extends Study {

  private def delete_temp(): Unit = {
    try {
      FileUtils.deleteDirectory(new File("temp"))
    }
  }

  private def getProjects: List[Project] = {
    val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build()
    val content: Try[String] = Using(Source.fromFile("projects.json")) { _.mkString }

    if (content.isFailure) {
      println("Project list don't exists")
      return List[Project]()
    }

    try {
      mapper.readValue(content.get, new TypeReference[List[Project]] {})
    } catch {
      case _: Exception =>
        println("Error processing json. Make sure to well formatting the json file, with all fields")
        List[Project]()
    }
  }

  @tailrec
  private def executeNextProject(projects:List[Project]): Unit = {
    if (projects.isEmpty) return
    ProjectFilter.buildFilter(projects.head.name)
    mineRepo(projects.head)
    executeNextProject(projects.tail)
  }

  private def mineRepo(project:Project): Unit = {
    delete_temp()

    val reportName = "output" + File.separator + project.name + "_report.csv"
    val fileWriter = new FileWriter(reportName)
    try {
      fileWriter.append(
        "url," +
        "old_path,"+
        "new_path,"+
        "old_loc,"+
        "new_loc,"+
        "old_block,"+
        "new_block,"+
        "old_disciplined,"+
        "new_disciplined,"+
        "old_undisciplined,"+
        "new_undisciplined,"+
        "commit_hash,"+
        "author\n"
      )
    } catch {
      case _: Exception =>
        println("Error to write titles in the report file. skipping project " + project.name)
        return
    } finally {
      fileWriter.close()
    }

    val scmRepo = GitRemoteRepository.hostedOn(project.url)
      .inTempDir("temp")
      .buildAsSCMRepository()
    val range = Commits.range(project.startCommit, project.endCommit)
    val commits = range.get(scmRepo.getScm)

    ProjectFilter.totalCommits = commits.size()
    ProjectFilter.countCommits = 0

    var repositoryMining = new RepositoryMining()
    repositoryMining = repositoryMining.in(scmRepo)

    if (project.withThreads) {
      repositoryMining = repositoryMining
      .visitorsAreThreadSafe(true)
      .visitorsChangeRepoState(true)
      .withThreads()
    }

    repositoryMining
      .through(range)
      .process(new MyVisitor(), new CSVFile(reportName, true))
      .mine()

    delete_temp()
  }

  override def execute(): Unit = {
    executeNextProject(getProjects)
  }
}
