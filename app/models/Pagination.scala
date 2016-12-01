package models

import play.api.db.DB
import sorm.Persisted

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}
/**
  * Created by gabriel on 11/14/16.
  */
object Pagination{

  def list(page: Int = 0, pageSize: Int = 9, orderBy: Int = 1, filter: String = "%"):Page[Items with Persisted] = {

    val offest = pageSize * page

    val items = (for (n <- DB.query[Items].limit(pageSize).whereLike("name", filter).offset(offest).fetch) yield {n}).toList

    val totalRows = DB.query[Items].fetch.size

    Page(items, page, offest, totalRows)
  }

}
