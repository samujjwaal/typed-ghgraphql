package com.ashessin.cs474.project.builder

import com.ashessin.cs474.project.model.{GHSearch, GHSearchBuilder}
import org.joda.time.DateTime

import scala.collection.mutable

/**
 * Github Issue/PR search class.
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in issue/pr search type
 */
class IssuePrSearch(
  keyword: String = "",
  qualifiers: mutable.Map[(String, String), Set[_]] = mutable.HashMap[(String, String), Set[_]]()
) extends GHSearch(keyword, qualifiers) {
}

/**
 * Builder class for [[com.ashessin.cs474.project.builder.IssuePrSearch]]
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in issue/pr search type
 */
class IssuePrSearchBuilder private(keyword: String, qualifiers: mutable.Map[(String, String), Set[_]])
  extends GHSearchBuilder(keyword, qualifiers) {


  /**
   * Restrict search results to 'issues' or 'pull requests'
   *
   * @param queryType to choose between 'issues' and 'pull requests'
   * @return Issue/PR Builder instance
   */
  def queryType(queryType: String): IssuePrSearchBuilder = {
    queryType.toLowerCase() match {
      case "pr" =>
      case "issue" =>
      case _ => throw new IllegalStateException("Valid arguments are: pr, issue");
    }
    updateQuery("is", queryType)
    this
  }

  /**
   * Search by title
   *
   * @return Issue/PR Builder instance
   */
  def inTitle(): IssuePrSearchBuilder = {
    updateQuery("in", "title")
    this
  }

  /**
   * Search by body
   *
   * @return Issue/PR Builder instance
   */
  def inBody(): IssuePrSearchBuilder = {
    updateQuery("in", "body")
    this
  }

  /**
   * Search by comments
   *
   * @return Issue/PR Builder instance
   */
  def inComments(): IssuePrSearchBuilder = {
    updateQuery("in", "comments")
    this
  }

  /**
   * Search all repositories owned by a certain user
   *
   * @param username the username to be searched for
   * @return Issue/PR Builder instance
   */
  def username(username: String): IssuePrSearchBuilder = {
    updateQuery("user", username)
    this
  }

  /**
   * Search all repositories owned by a certain organization
   *
   * @param orgname the organization name to be searched for
   * @return Issue/PR Builder instance
   */
  def orgname(orgname: String): IssuePrSearchBuilder = {
    updateQuery("org", orgname)
    this
  }

  /**
   * Search in a specific repository
   *
   * @param owner the username of the owner of repository
   * @param name  the name of the repository
   * @return Issue/PR Builder instance
   */
  def repo(owner: String, name: String): IssuePrSearchBuilder = {
    updateQuery("repo", owner + "/" + name)
    this
  }

  /**
   * Filter search based on 'open' or 'closed' status
   *
   * @param state the state chosen for the issue/PR
   * @return Issue/PR Builder instance
   */
  def state(state: String): IssuePrSearchBuilder = {
    state.toLowerCase() match {
      case "open" =>
      case "closed" =>
      case _ => throw new IllegalStateException("Valid arguments are: open, closed");
    }
    updateQuery("state", state)
    this
  }

  /**
   * Filter search based on 'public' or 'private' repositories
   *
   * @param access the access level of the repository
   * @return Issue/PR Builder instance
   */
  def access(access: String): IssuePrSearchBuilder = {
    access.toLowerCase() match {
      case "public" =>
      case "private" =>
      case _ => throw new IllegalStateException("Valid arguments are: public, private");
    }
    updateQuery("is", access)
    this
  }

  /**
   * Search issues/PRs authored by a specific user
   *
   * @param username the username to be searched for
   * @return Issue/PR Builder instance
   */
  def author(username: String): IssuePrSearchBuilder = {
    updateQuery("author", username)
    this
  }

  /**
   * Filter issues/PRs assigned to a specific user
   *
   * @param username the username to be searched for
   * @return Issue/PR Builder instance
   */
  def assignedTo(username: String): IssuePrSearchBuilder = {
    updateQuery("assignee", username)
    this
  }

  /**
   * Filter issues/PRs that mention(@) a specific user
   *
   * @param username the username to be searched for
   * @return Issue/PR Builder instance
   */
  def mentionsUser(username: String): IssuePrSearchBuilder = {
    updateQuery("mentions", username)
    this
  }

  /**
   * Filter issues/PRs that mention(@) a specific team within an organization
   *
   * @param orgname  the organization name
   * @param teamname the team name to be searched for
   * @return Issue/PR Builder instance
   */
  def mentionsTeam(orgname: String, teamname: String): IssuePrSearchBuilder = {
    updateQuery("team", orgname + "/" + teamname)
    this
  }

  /**
   * Search issues containing a comment from a specific user
   *
   * @param username the username to be searched for
   * @return Issue/PR Builder instance
   */
  def commenter(username: String): IssuePrSearchBuilder = {
    updateQuery("commenter", username)
    this
  }

  /**
   * Search issues/PRs that in some way involve a certain user
   * 'involves' qualifier is a logical OR between author, assignee, mentions, and commenter qualifiers for a single user
   *
   * @param username the username to be searched for
   * @return Issue/PR Builder instance
   */
  def involves(username: String): IssuePrSearchBuilder = {
    updateQuery("involves", username)
    this
  }

  /**
   * Filter issues by their labels
   *
   * @param labelname the issue label to filter by
   * @return Issue/PR Builder instance
   */
  def label(labelname: String): IssuePrSearchBuilder = {
    updateQuery("involves", labelname)
    this
  }

  /**
   * Filter PRs based on status of the commits
   *
   * @param status the PR status to filter by
   * @return Issue/PR Builder instance
   */
  def commitStatus(status: String): IssuePrSearchBuilder = {
    status.toLowerCase() match {
      case "pending" =>
      case "success" =>
      case "failure" =>
      case _ => throw new IllegalStateException("Valid arguments are: pending, success, failure");
    }
    updateQuery("is", status)
    this
  }

  /**
   * Filter PRs by the branch they come from
   *
   * @param branchname the branch name to be searched for
   * @return Issue/PR Builder instance
   */
  def headBranch(branchname: String): IssuePrSearchBuilder = {
    updateQuery("head", branchname)
    this
  }

  /**
   * Filter PRs by the branch they are merging into
   *
   * @param branchname the branch name to be searched for
   * @return Issue/PR Builder instance
   */
  def baseBranch(branchname: String): IssuePrSearchBuilder = {
    updateQuery("base", branchname)
    this
  }

  /**
   * Filter issues/PRs within repositories written in a certain language
   *
   * @param language the language to look for in the users' repository
   * @return Issue/PR Builder instance
   */
  def language(language: String): IssuePrSearchBuilder = {
    updateQuery("language", language)
    this
  }

  /**
   * Filter issues by the number of comments
   *
   * @param comments the number of comments for the issue to filter by
   * @param op       the operator to use <,>,<=,>=,= qualifiers
   * @return Issue/PR Builder instance
   */
  def commentsCount(comments: Int, op: String): IssuePrSearchBuilder = {
    updateQuery(("comments", op), comments)
    this
  }

  /**
   * Filter by when issues/PRs based on time of creation
   *
   * @param created the date of issue creation in ISO8601 standard format
   * @param op      the operator to use <,>,<=,>=,= qualifiers
   * @return Issue/PR Builder instance
   */
  def created(created: DateTime, op: String): IssuePrSearchBuilder = {
    updateQuery(("created", op), created)
    this
  }

  /**
   * Filter by when issues/PRs based on time of last updated
   *
   * @param updated the date of last updation in ISO8601 standard format
   * @param op      the operator to use <,>,<=,>=,= qualifiers
   * @return Issue/PR Builder instance
   */
  def updated(updated: DateTime, op: String): IssuePrSearchBuilder = {
    updateQuery(("updated", op), updated)
    this
  }

  /**
   * Filter by when issues/PRs based on when they were closed
   *
   * @param closed the date when issue was closed in ISO8601 standard format
   * @param op     the operator to use <,>,<=,>=,= qualifiers
   * @return Issue/PR Builder instance
   */
  def closed(closed: DateTime, op: String): IssuePrSearchBuilder = {
    updateQuery(("closed", op), closed)
    this
  }

  /**
   * Filter by when issues/PRs based on when they were merged
   *
   * @param merged the date PR was last merged in ISO8601 standard format
   * @param op     the operator to use <,>,<=,>=,= qualifiers
   * @return Issue/PR Builder instance
   */
  def merged(merged: DateTime, op: String): IssuePrSearchBuilder = {
    updateQuery(("merged", op), merged)
    this
  }

  /**
   * Search based on whether PRs are merged or unmerged
   *
   * @param status the merge status of PR to filter by
   * @return Issue/PR Builder instance
   */
  def mergeStatus(status: String): IssuePrSearchBuilder = {
    status.toLowerCase() match {
      case "merged" =>
      case "unmerged" =>
      case _ => throw new IllegalStateException("Valid arguments are: merged, unmerged");
    }
    updateQuery("is", status)
    this
  }

  /**
   * Filter issues/PRs based on if they are in archived repositories
   *
   * @param status the archive status of the repository
   * @return Issue/PR Builder instance
   */
  def archivedStatus(status: Boolean): IssuePrSearchBuilder = {
    updateQuery("archived", status)
    this
  }

  def build(): IssuePrSearch = {
    new IssuePrSearch(this.keyword, this.qualifiers)
  }

  protected def this() = this(
    "", mutable.HashMap[(String, String), Set[_]]()
  )

  protected def this(keyword: String) = this(
    keyword, mutable.HashMap[(String, String), Set[_]]()
  )
}

object IssuePrSearchBuilder {
  def apply(keyword: String) = new IssuePrSearchBuilder(keyword)

  def apply() = new IssuePrSearchBuilder()
}
