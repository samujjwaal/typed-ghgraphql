package com.ashessin.cs474.project.builder

import com.ashessin.cs474.project.model.{GHSearch, GHSearchBuilder}
import org.joda.time.DateTime

import scala.collection.mutable

/**
 * Github User search class.
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in user search type
 */
class UserSearch(
  keyword: String = "",
  qualifiers: mutable.Map[(String, String), Set[_]] = mutable.HashMap[(String, String), Set[_]]()
) extends GHSearch(keyword, qualifiers) {
}

/**
 * Builder class for [[com.ashessin.cs474.project.builder.UserSearch]]
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in issue/pr search type
 */
class UserSearchBuilder private(keyword: String, qualifiers: mutable.Map[(String, String), Set[_]])
  extends GHSearchBuilder(keyword, qualifiers) {

  /**
   * Restrict search results to personal accounts or organizations only.
   *
   * @param accountType type of accounts to be searched
   * @return UserBuilder instance
   */
  def is(accountType: String): UserSearchBuilder = {
    accountType.toLowerCase() match {
      case "user" =>
      case "org" =>
      case _ => throw new IllegalStateException("Valid arguments are: user, org");
    }
    updateQuery("type", accountType)
    this
  }

  /**
   * Matches search results by a specific username/account name.
   *
   * @param username username to be searched for
   * @return UserBuilder instance
   */
  def user(username: String): UserSearchBuilder = {
    updateQuery("user", username)
    this
  }

  /**
   * Matches search results by a specific organization's account name.
   *
   * @param orgname organization name to be searched for
   * @return UserBuilder instance
   */
  def org(orgname: String): UserSearchBuilder = {
    updateQuery("org", orgname)
    this
  }

  /**
   * Matches users by searching text in their usernames.
   *
   * @return UserBuilder instance
   */
  def inLogin(): UserSearchBuilder = {
    updateQuery("in", "login")
    this
  }

  /**
   * Matches users by searching text in their real names.
   *
   * @return UserBuilder instance
   */
  def inName(): UserSearchBuilder = {
    updateQuery("in", "name")
    this
  }

  /**
   * Matches users by a specific fullname.
   *
   * @param firstname first name
   * @param lastname  last name
   * @return UserBuilder instance
   */
  def fullName(firstname: String, lastname: String): UserSearchBuilder = {
    updateQuery("fullname", firstname + " " + lastname)
    this
  }

  /**
   * Matches users by searching text in their email
   *
   * @return UserBuilder instance
   */
  def inEmail(): UserSearchBuilder = {
    updateQuery("in", "email")
    this
  }

  /**
   * Filter search by number of repositories a user owns.
   *
   * @param n  number of repositories for the target user
   * @param op operator to use <,>,<=,>=,= qualifiers
   * @return UserBuilder instance
   */
  def repos(n: Int, op: String): UserSearchBuilder = {
    updateQuery(("repos", op), n)
    this
  }

  /**
   * Search for users by users' location
   *
   * @param location location indicated in user's profile
   * @return UserBuilder instance
   */
  def location(location: String): UserSearchBuilder = {
    updateQuery("location", location)
    this
  }

  /**
   * Filter users based on languages present in their repositories
   *
   * @param language language to look for in the users' repository
   * @return UserBuilder instance
   */
  def language(language: String): UserSearchBuilder = {
    updateQuery("location", language)
    this
  }

  /**
   * Filter users by their number of followers
   *
   * @param followers number of followers for the target user
   * @param op        operator to use <,>,<=,>=,= qualifiers
   * @return UserBuilder instance
   */
  def followers(followers: Int, op: String): UserSearchBuilder = {
    updateQuery(("followers", op), followers)
    this
  }

  /**
   * Search by when a user joined GitHub
   *
   * @param created date of account creation in ISO8601 standard format
   * @param op      operator to use <,>,<=,>=,= qualifiers
   * @return UserBuilder instance
   */
  def created(created: DateTime, op: String): UserSearchBuilder = {
    updateQuery(("created", op), created)
    this
  }

  def build(): UserSearch = {
    new UserSearch(this.keyword, this.qualifiers)
  }

  protected def this() = this(
    "", mutable.HashMap[(String, String), Set[_]]()
  )

  protected def this(keyword: String) = this(
    keyword, mutable.HashMap[(String, String), Set[_]]()
  )

}

object UserSearchBuilder {
  def apply(keyword: String) = new UserSearchBuilder(keyword)

  def apply() = new UserSearchBuilder()
}
