package com.ashessin.cs474.project.builder

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite

import scala.util.matching.Regex

class GitHubQueryBuilderTest extends FunSuite {

  val token: String = ConfigFactory.load().getString("GITHUB_TOKEN")

  // Build and execute a very narrow repository search query to verify correct repository is fetched
  test("Repository search result validation test") {
    val result = GitHubQueryBuilder()
      .withSearch(RepositorySearchBuilder("spark-stocksim")
        .repo("user501254", "spark-stocksim")
        .topic("spark-sql")
        .topics(2, ">")
        .build())
      .withLimit(1)
      .withToken(token)
      .build()

    // crate date for "user501254/spark-stocksim" repo
    val crateDate = "2019-11-13T05:36:34Z".r
    assert(crateDate.findFirstMatchIn(result.execute().left.getOrElse("")).get.group(0) == crateDate.toString())
  }

  test("Repository search result pagination test") {
    val cursorPattern: Regex = "\"endCursor\":\"(.*)\"},\"nodes\":".r

    val result = GitHubQueryBuilder()
      .withSearch(RepositorySearchBuilder()
        .user("user501254")
        .topics(1, ">")
        .build())
      .withLimit(1)
      .withToken(token)
      .build()

    val endCursor1 = Option(cursorPattern.findFirstMatchIn(result.execute().left.getOrElse("null")).get.group(1))
    val endCursor2 = Option(cursorPattern.findFirstMatchIn(result.execute().left.getOrElse("null")).get.group(1))

    assert(endCursor1.isDefined && endCursor2.isDefined)
    assert(endCursor1 != "null")
    assert(endCursor2 != "null")
    assert(endCursor1 != endCursor2)
  }

  test("Users search having many followers") {
    val result = GitHubQueryBuilder().withSearch(UserSearchBuilder()
      .followers(100000, ">")
      .build())
      .withLimit(15)
      .withToken(token)
      .build()

    // should most likely contain Linus Torvalds
    val x= "Linus Torvalds".r
    assert(x.findFirstMatchIn(result.execute().left.getOrElse("")).get.group(0) == x.toString())
  }

  test("Users search with 0 results") {
    val result = GitHubQueryBuilder().withSearch(UserSearchBuilder()
      .followers(-100, "<")
      .build())
      .withLimit(15)
      .withToken(token)
      .build()

    assert(result.execute().isRight) //Right(-1)
  }

  test("Repository search query builder test") {
    assertThrows[IllegalStateException] {
      RepositorySearchBuilder("spark-stocksim")
        .repo("user501254", "spark-stocksim")
        .topic("spark-sql")
        .topics(2, "")
        .build()
    }
  }

  test("Root query builder test") {
    assertThrows[IllegalStateException] {
      GitHubQueryBuilder()
        .withSearch(RepositorySearchBuilder("spark-stocksim")
          .repo("user501254", "spark-stocksim")
          .topic("spark-sql")
          .topics(2, ">")
          .build())
        .withLimit(-1)
        .withToken(" ")
    }
  }
}
