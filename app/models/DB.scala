package models

import sorm._

case class User(name: String, password: String)
case class Items(name: String, code: String, price: Double, description: String)
case class Order(name: String, address: String, pDay: String, item: Seq[Map[String,Int]], price: Double)
case class Orders(name: String, address: String, pDay: String, item: Seq[Map[String,Int]], price: Double)
case class Stocks(name: String, quantity: Int, sold: Int, returned: Int)

object DB extends Instance(entities = Seq(Entity[Order](), Entity[Orders](), Entity[Items](), Entity[Stocks]()),
  url = "jdbc:postgresql://	bmqjvn5gqpjrypr-postgresql.services.clever-cloud.com/bmqjvn5gqpjrypr",
  user = "	u3cktkz8my1gahwseumx",
  password = "sFXgPfQc9UvnoJ1di3kG",
  initMode = InitMode.Create
)
