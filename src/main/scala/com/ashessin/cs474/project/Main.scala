package com.ashessin.cs474.project

import com.ashessin.cs474.project.builder.{GitHubQueryBuilder, RepositorySearchBuilder}
import com.typesafe.config.ConfigFactory

object Main extends App {

  val token = ConfigFactory.load().getString("GITHUB_TOKEN")

  // Conventional Builder Pattern
  val javaRepositoryWithManyFormks = RepositorySearchBuilder()
    .language("java") // available language list changes frequently, so no checks
    .forks(1000, ">")
    .build()

  // Builder pattern with Phantom Types
  var queryContainer = GitHubQueryBuilder().withSearch(javaRepositoryWithManyFormks)
    .withLimit(10)
    .withToken(token)
    .build()


  // pagniate 3 pages
  println(queryContainer.execute())
  println(queryContainer.execute())
  println(queryContainer.execute())
}