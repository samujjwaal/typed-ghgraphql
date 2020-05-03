package com.ashessin.cs474.project.model

import scala.collection.mutable

/**
 * Base class for all github search builders.
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in a given github search type (repository, user ...)
 */
abstract class GHSearchBuilder(var keyword: String, protected var qualifiers: mutable.Map[(String, String), Set[_]]) {

  /**
   * Validates keys that have range operators and updates search query string.
   *
   * @param key   a tuple where first element is the key and second is the operator
   * @param value content to the right of colon
   */
  protected def updateQuery(key: (String, String), value: Any): Unit = {
    key._2 match {
      case ">" => ;
      case ">=" => ;
      case "<" => ;
      case "<=" => ;
      case "=" => ;
      case _ => throw new IllegalStateException("Valid operators are: >, >=, <, <=, =");
    }
    this.qualifiers.get(key) match {
      case Some(_) => this.qualifiers.put(key, this.qualifiers(key) ++ Set(value))
      case None => this.qualifiers.put(key, Set(value))
    }
  }

  /**
   * Overloaded convenience method for
   * `com.ashessin.cs474.project.model.GHSearchBuilder#updateQuery(java.lang.String, java.lang.Object)`
   *
   * @param key   the search key
   * @param value content to the right of colon (value for the key)
   */
  protected def updateQuery(key: String, value: Any): Unit = {
    this.qualifiers.get(key, "") match {
      case Some(_) => this.qualifiers.put((key, ""), this.qualifiers(key, "") ++ Set(value))
      case None => this.qualifiers.put((key, ""), Set(value))
    }
  }
}
