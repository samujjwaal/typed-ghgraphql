package com.ashessin.cs474.project

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import com.ashessin.cs474.project.graphql.client.Github.{Query, Repository, RepositoryConnection, User}
import sttp.client._
import sttp.client.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.console.putStrLn
import zio.{ZIO, _}

object Main extends App {
  val t = User.repositories(first = Option(10)) {RepositoryConnection.nodes(Repository.nameWithOwner)}
  val query: SelectionBuilder[RootQuery, Any] = Query.user(login = "samujjwaal") {
    User.name ~ User.bio ~
    t
  }



  println(s"Query: ${query.toGraphQL().query}")

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val uri = uri"https://api.github.com/graphql"
    val token = System.getenv("GITHUB_TOKEN")

    SttpClient
      .send(query.toRequest(uri).auth.bearer(token))
      .map(_.body)
      .absolve
      .tap(res => putStrLn(s"Result: $res"))
      .provideCustomLayer(AsyncHttpClientZioBackend.layer())
      .foldM(ex => putStrLn(ex.toString).as(1), _ => ZIO.succeed(0))
  }
}
