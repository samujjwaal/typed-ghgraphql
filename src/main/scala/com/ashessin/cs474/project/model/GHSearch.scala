package com.ashessin.cs474.project.model

import scala.collection.mutable

/**
 * Base class for all github search classes, instantiate using respective builder patterns.
 *
 * @param keyword    the search term to use
 * @param qualifiers the context of the search in a given github search type (repository, user ...)
 */
abstract class GHSearch(
  val keyword: String = "",
  val qualifiers: mutable.Map[(String, String), Set[_]] = mutable.HashMap[(String, String), Set[_]]()
) {
  override def toString: String = {
    var q = keyword
    qualifiers.foreach(qualifier => {
      q = q + " " + (qualifier._1._1 + ":" + qualifier._1._2 + qualifier._2.mkString(","))
    })
    q.trim
  }
}
