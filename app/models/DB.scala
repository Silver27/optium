package models

import sorm._

case class User(name: String, password: String)
case class Items(name: String, code: String, price: Double, description: String)
case class Order(name: String, address: String, pDay: String, item: Seq[Map[String,Int]], price: Double)
case class Orders(name: String, address: String, pDay: String, item: Seq[Map[String,Int]], price: Double)
case class Stocks(name: String, quantity: Int, sold: Int, returned: Int)

object DB extends Instance(entities = Seq(Entity[Order](), Entity[Orders](), Entity[Items](), Entity[Stocks]()),
  url = "jdbc:postgresql://localhost/play",
  user = "postgres",
  password = "optium",
  initMode = InitMode.Create
)
