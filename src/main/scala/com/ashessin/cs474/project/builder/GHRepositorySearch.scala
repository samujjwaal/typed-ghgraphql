package com.ashessin.cs474.project.builder

import com.ashessin.cs474.project.model.{GHSearch, GHSearchBuilder}
import org.joda.time.DateTime

import scala.collection.mutable

/**
 * Github Repository search class.
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in user search type
 */
class RepositorySearch(
  keyword: String = "",
  qualifiers: mutable.Map[(String, String), Set[_]] = mutable.HashMap[(String, String), Set[_]]()
) extends GHSearch(keyword, qualifiers) {
}

/**
 * Builder class for [[com.ashessin.cs474.project.builder.RepositorySearch]]
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in issue/pr search type
 */
class RepositorySearchBuilder private(keyword: String, qualifiers: mutable.Map[(String, String), Set[_]])
  extends GHSearchBuilder(keyword, qualifiers) {

  /**
   * Matches repositories by searching text in the repository names.
   *
   * @return RepositorySearchBuilder instance
   */
  def inName(): RepositorySearchBuilder = {
    updateQuery("in", "name")
    this
  }

  /**
   * Matches repositories by searching text in the repository description.
   *
   * @return RepositorySearchBuilder instance
   */
  def inDescription(): RepositorySearchBuilder = {
    updateQuery("in", "description")
    this
  }

  /**
   * Matches repositories by searching text in the repository README file.
   *
   * @return RepositorySearchBuilder instance
   */
  def inReadme(): RepositorySearchBuilder = {
    updateQuery("in", "readme")
    this
  }

  /**
   * Search for a specific repository
   *
   * @param owner the username of the owner of repository
   * @param name  the name of the repository
   * @return RepositorySearchBuilder instance
   */
  def repo(owner: String, name: String): RepositorySearchBuilder = {
    updateQuery("repo", owner + "/" + name)
    this
  }

  /**
   * Search all repositories owned by a certain user
   *
   * @param username the username to be searched for
   * @return RepositorySearchBuilder instance
   */
  def user(username: String): RepositorySearchBuilder = {
    updateQuery("user", username)
    this
  }

  /**
   * Search all repositories owned by a certain organization
   *
   * @param orgname the organization name to be searched for
   * @return RepositorySearchBuilder instance
   */
  def org(orgname: String): RepositorySearchBuilder = {
    updateQuery("org", orgname)
    this
  }

  /**
   * Matches repositories that have a specified size
   *
   * @param size the size of repository in 'KB', to search for
   * @param op   the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def size(size: Int, op: String): RepositorySearchBuilder = {
    updateQuery(("size", op), size)
    this
  }

  /**
   * Filter repositories by their number of followers
   *
   * @param followers the number of followers for the target repository
   * @param op        the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def followers(followers: Int, op: String): RepositorySearchBuilder = {
    updateQuery(("followers", op), followers)
    this
  }

  /**
   * Filter repositories by their number of forks
   *
   * @param forks the number of forks for target repository
   * @param op    the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def forks(forks: Int, op: String): RepositorySearchBuilder = {
    updateQuery(("forks", op), forks)
    this
  }

  /**
   * Filter repositories by their number of stars
   *
   * @param stars the number of stars for target repository
   * @param op    the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def stars(stars: Int, op: String): RepositorySearchBuilder = {
    updateQuery(("stars", op), stars)
    this
  }

  /**
   * Filter repositories based on time of creation
   *
   * @param created the date of repository creation in ISO8601 standard format
   * @param op      the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def created(created: DateTime, op: String): RepositorySearchBuilder = {
    updateQuery(("created", op), created)
    this
  }

  /**
   * Filter repositories based on time of pushing commits
   *
   * @param pushed the date of the pushed commit in ISO8601 standard format
   * @param op     the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def pushed(pushed: DateTime, op: String): RepositorySearchBuilder = {
    updateQuery(("pushed", op), pushed)
    this
  }

  /**
   * Filter repositories written in a certain language
   *
   * @param language the language to look for in the repository
   * @return RepositorySearchBuilder instance
   */
  def language(language: String): RepositorySearchBuilder = {
    updateQuery("language", language)
    this
  }

  /**
   * Matches repositories that have been classified with a specific topic
   *
   * @param topic the topic to be searched for
   * @return RepositorySearchBuilder instance
   */
  def topic(topic: String): RepositorySearchBuilder = {
    updateQuery("topic", topic)
    this
  }

  /**
   * Matches repositories that have a specific number of topics
   *
   * @param topics the number of topics to search for
   * @param op     the operator to use <,>,<=,>=,= qualifiers
   * @return RepositorySearchBuilder instance
   */
  def topics(topics: Int, op: String): RepositorySearchBuilder = {
    updateQuery(("topics", op), topics)
    this
  }

  /**
   * Matches repositories that are licensed under a specific license
   *
   * @param license the particular licence being searched
   * @return RepositorySearchBuilder instance
   */
  def license(license: String): RepositorySearchBuilder = {
    updateQuery("license", license)
    this
  }

  /**
   * Filter search based on 'public' or 'private' repositories
   *
   * @param access the access level of the repository
   * @return RepositorySearchBuilder instance
   */
  def is(access: String): RepositorySearchBuilder = {
    access match {
      case "private" =>
      case "public" =>
      case _ => throw new IllegalStateException("Valid arguments are: public, private");
    }
    updateQuery("is", access)
    this
  }

  /**
   * Filter search based on if a repository is a mirror
   *
   * @param mirror the mirror status of the repository
   * @return RepositorySearchBuilder instance
   */
  def mirror(mirror: Boolean): RepositorySearchBuilder = {
    updateQuery("mirror", mirror)
    this
  }

  /**
   * Matches repositories based on whether they are archived
   *
   * @param archived the archive status of the repository
   * @return RepositorySearchBuilder instance
   */
  def archived(archived: Boolean): RepositorySearchBuilder = {
    updateQuery("archived", archived)
    this
  }

  /**
   *
   * @param goodFirstIssues
   * @return RepositorySearchBuilder instance
   */
  def goodFirstIssues(goodFirstIssues: Int): RepositorySearchBuilder = {
    updateQuery(("good-first-issues", ">"), goodFirstIssues)
    this
  }

  /**
   *
   * @param helpWantedIssues
   * @return RepositorySearchBuilder instance
   */
  def helpWantedIssues(helpWantedIssues: Int): RepositorySearchBuilder = {
    updateQuery(("help-wanted-issues", ">"), helpWantedIssues)
    this
  }

  def build(): RepositorySearch = {
    new RepositorySearch(this.keyword, this.qualifiers)
  }

  protected def this() = this(
    "", mutable.HashMap[(String, String), Set[_]]()
  )

  protected def this(keyword: String) = this(
    keyword, mutable.HashMap[(String, String), Set[_]]()
  )

}

object RepositorySearchBuilder {
  def apply(keyword: String) = new RepositorySearchBuilder(keyword)

  def apply() = new RepositorySearchBuilder()
}
