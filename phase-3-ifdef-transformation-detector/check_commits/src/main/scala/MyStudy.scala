package ic.ufal.ifdefcatcher

import utils.{ConfigurationFile, Project, ProjectFilter}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.commons.io.FileUtils
import org.repodriller.filter.commit.{OnlyModificationsWithFileTypes, OnlyNoMerge}
import org.repodriller.filter.range.{CommitRange, Commits}
import org.repodriller.persistence.csv.CSVFile
import org.repodriller.scm.GitRemoteRepository
import org.repodriller.{RepositoryMining, Study}

import java.io.{File, FileWriter}
import java.nio.file.Paths
import java.util
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

    var range : CommitRange = null
    if (project.method.equals("tags") && project.startTag != null && project.endTag != null) {
      range = Commits.betweenTags(project.startTag, project.endTag)
    } else if (project.method.equals("commits") && project.startCommit != null && project.endCommit != null) {
      range = Commits.range(project.startCommit, project.endCommit)
    } else if (project.method.equals("betweenDates") && project.from != null && project.to != null) {
      println("aqui....")
      println("" + project.from + " " + project.to)
      range = Commits.betweenDates(project.from, project.to)
    } else if (project.method.equals("date") && project.since != null) {
      range = Commits.since(project.since)
    } else {
      println("Error to get project " + project.name + " configuration. Exiting...")
      return
    }
    val commits = range.get(scmRepo.getScm)

    ProjectFilter.totalCommits = commits.size()
    ProjectFilter.countCommits = 0

    var repositoryMining = new RepositoryMining()
    repositoryMining = repositoryMining.in(scmRepo)

    if (project.withThreads) {
      repositoryMining = repositoryMining
        .visitorsAreThreadSafe(true)
        .visitorsChangeRepoState(true)
        .withThreads(4)
    }

    repositoryMining
      .filters(
        new OnlyNoMerge(),
        new OnlyModificationsWithFileTypes(util.Arrays.asList(".c", ".C", ".h", ".H"))
      )
      .through(range)
      .process(new MyVisitor(), new CSVFile(reportName, true))
      .mine()

    delete_temp()
  }

  override def execute(): Unit = {
    if (ConfigurationFile.getProperties) {
      ProjectFilter.TURN_ON = ConfigurationFile.getFilterOn
      ProjectFilter.FILEPATH = ConfigurationFile.getFilterPath
    }

    val outputFile = new File("output")
    if (!outputFile.exists()) { outputFile.mkdirs() }
    val cppStatsFile = new File("CPPSTATS")
    if (!cppStatsFile.exists()) { cppStatsFile.mkdirs() }

    executeNextProject(getProjects)
  }
}
