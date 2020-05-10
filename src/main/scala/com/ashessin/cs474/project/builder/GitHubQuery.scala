package com.ashessin.cs474.project.builder

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import com.ashessin.cs474.project.graphql.client.Github._
import com.ashessin.cs474.project.model.GHSearch
import org.slf4j.LoggerFactory
import scalaj.http.Http
import sttp.client._

import scala.util.matching.Regex

/**
 * The root query class for using Github's GraphQL API with typechecking.
 *
 * @param ghSearch instance of [[com.ashessin.cs474.project.model.GHSearch]] derived type
 * @param limit    number of results to fetch on each call to
 *                 [[com.ashessin.cs474.project.builder.GitHubQuery#execute()]]
 * @param token    the user's unique GitHub OAuth token
 * @param uri      uri to GitHub's GraphQl endpoint
 */
class GitHubQuery(
  val ghSearch: GHSearch,
  val limit: Int = 10,
  val token: String = "",
  val uri: String = "https://api.github.com/graphql"
) {

  private val logger = LoggerFactory.getLogger(classOf[GitHubQuery])

  private val dataPattern: Regex = ".*--data (.*) 'https:\\/\\/api\\.github\\.com\\/graphql'".r
  private val cursorPattern: Regex = "\"endCursor\":\"(.*)\"},\"nodes\":".r
  var graphqlQuery: Option[SelectionBuilder[RootQuery, _]] = None: Option[SelectionBuilder[RootQuery, _]]
  private var graphqlQueryRoot: Option[SelectionBuilder[SearchResultItemConnection, _] => SelectionBuilder[RootQuery, _]] =
    None: Option[SelectionBuilder[SearchResultItemConnection, _] => SelectionBuilder[RootQuery, _]]
  private var endCursor: Option[String] = None: Option[String]


  /**
   * Executes a request to the GitHub's GraphQl endpoint, also paginates results on subsequent calls.
   *
   * @return the graphql response as string if successful, else -1
   */
  def execute(): Either[String, Int] = {
    ghSearch match {
      case _: RepositorySearch =>
        graphqlQueryRoot = Option(Query.search(first = Option(limit), query = ghSearch.toString(),
          `type` = SearchType.REPOSITORY, after = endCursor))
        searhRepositoryGraphQL()
      case _: UserSearch =>
        graphqlQueryRoot = Option(Query.search(first = Option(limit), query = ghSearch.toString(),
          `type` = SearchType.USER, after = endCursor))
        searhUserGraphQL()
      case _: UserSearch =>
        graphqlQueryRoot = Option(Query.search(first = Option(limit), query = ghSearch.toString(),
          `type` = SearchType.ISSUE, after = endCursor))
        searhIssuePrGraphQL()
    }

    try {
      val data = dataPattern.findFirstMatchIn(graphqlQuery.get.toRequest(uri"${uri}").toCurl).get.group(1).dropRight(1).drop(1)
      logger.info(s"data: ${data}")

      val request = Http(uri)
        .header("Content-Type", "application/json; charset=utf-8")
        .header("Authorization", "Bearer " + token)
        .postData(data)
        .asString

      endCursor = Option(cursorPattern.findFirstMatchIn(request.body).get.group(1))
      logger.info(s"endCursor: ${endCursor}")

      if (endCursor.isDefined && !endCursor.get.equals("null")) {
        Left(request.body)
      } else {
        Right(0)
      }
    } catch {
      case _: Throwable => Right(-1)
    }

  }

  private def searhRepositoryGraphQL(): Unit = {
    val fields = {
      Repository.id ~
        Repository.nameWithOwner ~
        Repository.createdAt ~ Repository.description ~ Repository.homepageUrl ~
        Repository.forkCount ~ Repository.isArchived ~ Repository.isFork ~ Repository.isPrivate ~
        Repository.stargazers() {
          StargazerConnection.totalCount
        } ~
        Repository.languages(first = Option(3)) {
          LanguageConnection.nodes({
            Language.name
          })
        }
    }
    graphqlQuery = Option(graphqlQueryRoot.get {
      SearchResultItemConnection.pageInfo(PageInfo.endCursor) ~
        SearchResultItemConnection.nodes(
          onApp = App.id,
          onIssue = Issue.id,
          onMarketplaceListing = MarketplaceListing.id,
          onOrganization = Organization.id,
          onPullRequest = PullRequest.id,
          onRepository = fields,
          onUser = User.id)
    })
  }

  private def searhUserGraphQL(): Unit = {
    val fields = {
      User.id ~
        User.login ~ User.name ~ User.bio ~ User.company ~ User.email ~ User.location ~ User.websiteUrl ~
        User.followers() {
          FollowerConnection.totalCount
        }
    }
    graphqlQuery = Option(graphqlQueryRoot.get {
      SearchResultItemConnection.pageInfo(PageInfo.endCursor) ~
        SearchResultItemConnection.nodes(
          onApp = App.id,
          onIssue = Issue.id,
          onMarketplaceListing = MarketplaceListing.id,
          onOrganization = Organization.id,
          onPullRequest = PullRequest.id,
          onRepository = Repository.id,
          onUser = fields)
    })
  }

  private def searhIssuePrGraphQL(): Unit = {
    val fields = {
      Issue.id ~
        Issue.title ~ Issue.createdAt ~ Issue.bodyText ~ Issue.closed ~ Issue.lastEditedAt ~ Issue.url
    }
    graphqlQuery = Option(graphqlQueryRoot.get {
      SearchResultItemConnection.pageInfo(PageInfo.endCursor) ~
        SearchResultItemConnection.nodes(
          onApp = App.id,
          onIssue = fields,
          onMarketplaceListing = MarketplaceListing.id,
          onOrganization = Organization.id,
          onPullRequest = PullRequest.id,
          onRepository = Repository.id,
          onUser = User.id)
    })
  }

  override def toString = s"GitHubQuery(${this.graphqlQuery.get.toGraphQL()})"
}

sealed trait QueryBuilderStep

sealed trait HasSearch extends QueryBuilderStep

sealed trait HasLimit extends QueryBuilderStep

sealed trait HasToken extends QueryBuilderStep


/**
 * Builder class using Phantom Types for instantiating [[com.ashessin.cs474.project.builder.GitHubQuery]].
 *
 * @param ghSearch instance of [[com.ashessin.cs474.project.model.GHSearch]] derived type
 * @param limit    number of results to fetch on each call to
 *                 [[com.ashessin.cs474.project.builder.GitHubQuery#execute()]]
 * @param token    the user's unique GitHub OAuth token
 * @param uri      uri to GitHub's GraphQl endpoint
 * @tparam PassedStep via HasSearch, HasLimit, HasToken
 */
class GitHubQueryBuilder[PassedStep <: QueryBuilderStep] private(
  var ghSearch: GHSearch,
  var limit: Int,
  var token: String,
  var uri: String
) {
  private val logger = LoggerFactory.getLogger("GitHubQueryBuilder")

  /**
   * Sets the search type, one of (repository, user, issue/pr).
   *
   * @param search instance of [[com.ashessin.cs474.project.model.GHSearch]] derived type
   * @return an instance of this builder
   */
  def withSearch(search: GHSearch): GitHubQueryBuilder[HasSearch] = {
    this.ghSearch = search
    new GitHubQueryBuilder[HasSearch](this)
  }

  protected def this(gqb: GitHubQueryBuilder[_]) = this(
    gqb.ghSearch,
    gqb.limit,
    gqb.token,
    gqb.uri
  )

  /**
   * Sets the number of results to fetch on each page of response.
   *
   * @param limit number of results to fetch
   * @param ev    implicit Phantom Type checking
   * @return an instance of this builder
   */
  def withLimit(limit: Int)(implicit ev: PassedStep =:= HasSearch): GitHubQueryBuilder[HasLimit] = {
    if (limit <= 0) {
      this.limit = 1
    } else {
      this.limit = limit
    }
    new GitHubQueryBuilder[HasLimit](this)
  }

  /**
   * Sets the OAuth token to use with the request.
   *
   * @param token the user's unique GitHub OAuth token
   * @param ev    implicit Phantom Type checking
   * @return an instance of this builder
   */
  def withToken(token: String)(implicit ev: PassedStep =:= HasLimit): GitHubQueryBuilder[HasToken] = {
    try {
      token.trim match {
        case "" => throw new IllegalStateException("Invalid token provided")
        case _ =>
      }
    } catch {
      case _: NullPointerException => logger.error("Github OAuth token was not provide. " +
        "Please check config and environment variables")
    }
    this.token = token
    new GitHubQueryBuilder[HasToken](this)
  }

  def build()(implicit ev: PassedStep =:= HasToken): GitHubQuery = new GitHubQuery(
    ghSearch, limit, token, uri
  )

  protected def this() = this(ghSearch = null, limit = 10, token = "", uri = "https://api.github.com/graphql")

  private def withURI(uri: String): GitHubQueryBuilder[PassedStep] = {
    this.uri = uri
    this
  }
}

object GitHubQueryBuilder {
  def apply() = new GitHubQueryBuilder[QueryBuilderStep]()
}
