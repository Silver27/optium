package controllers


import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import sorm.Persisted
import play.api.data.format.Formats._
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.ClientConfiguration
/**
  * Created by gabriel on 11/5/16.
  */
class DBController extends Controller{

  val imgBucket = "product-images"
  val access = "AI_E8-PMD2FJZILDUDPF"
  val secret = "XhrMKfFKa8UJf81Pavg7dtkp9npuCTPmXq3EfQ=="

  val opts = new ClientConfiguration()
  opts.setSignerOverride("S3SignerType")

  val aws_credentials = new BasicAWSCredentials(access, secret)
  val client = new AmazonS3Client(aws_credentials, opts)

  client.setEndpoint("cellar.services.clever-cloud.com")

  var o1 = ""
  var o2 = ""
  var o3 = ""
  var o4: Seq[String] = Seq()
  var o5: Seq[Int] = Seq()

  var orderTemp: (String, String, String, Seq[String], Seq[Int]) = (o1, o2, o3, o4, o5)
  var orderReset:(String, String, String, Seq[String], Seq[Int]) = (o1, o2, o3, o4, o5)

  val searchForm = Form{single("search" -> text)}

  val one = Form{single("stat" -> list(number))}

  val stockEdit = Form{
    tuple(
      "quantity" -> list(number),
      "returned" -> list(number),
      "id" -> list(number)
    )
  }

  val filterForm = Form{
    tuple(
      "name" -> text,
      "address" -> text,
      "pDay" -> text,
      "item" -> seq(text),
      "nItems" -> seq(number)
    )
  }

  val itemForm: Form[Items] = Form{
    mapping(
      "name" -> text,
      "code" -> text,
      "price" -> of[Double],
      "description" -> text
    )(Items.apply)(Items.unapply)
  }

  def next(page: Int, orderBy: Int, filter: String)= Action{implicit request =>
    var order = filterForm.bindFromRequest.get
    orderTemp = (order._1, order._2, order._3, orderTemp._4 ++ order._4, orderTemp._5 ++ order._5)
    println(orderTemp)
    Ok(views.html.products(Pagination.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")), orderBy, filter))
  }

  def addOrder(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    val ord = filterForm.bindFromRequest.get
    val o = orderTemp
    val fQuantity = ord._5.filter(x => x != null)
    val fItem = for (n <- ord._4) yield {n.split(",").toSeq(0)}
    println(fItem)
    println(fQuantity)

    val price = (for(n <- 0 until fQuantity.size)yield{
      DB.query[Items].whereEqual("name", fItem(n)).fetchOne.get.price * fQuantity(n)
    }).sum

    val m = for (n <- 0 to ord._4.length - 1) yield {Map(ord._4(n).split(",")(0) -> ord._5(n))}

    val order = if (o._1 != "" && o._2 != "" && o._3 != "") {Order.apply(o._1, o._2, o._3, m, price)}else{Order.apply(ord._1, ord._2, ord._3, m, price)}

    orderTemp = orderReset

      DB.save(order)

      println(order)

      Ok(views.html.products(Pagination.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")), orderBy, filter))
  }

  def deleteOrder = Action { implicit request =>
    if (request.cookies.get("name").get.value.equals("manager") && request.cookies.get("password").get.value.equals("manager")) {
      val search = (for(n <- DB.query[Orders].fetch() if n.name.contains(searchForm.bindFromRequest.get)){DB.delete(n)})
      Redirect(routes.HomeController.index())
    } else {
      Redirect(routes.HomeController.dashBoard())
    }
  }

  def addItem = Action (parse.multipartFormData) {implicit request =>

    val picture = request.body.file("picture").get.ref.file

    val items = itemForm.bindFromRequest.get

    val modItem = Items.apply(items.name, items.code + client.getUrl(imgBucket, access), items.price, items.description)

    val stocks = Stocks.apply(items.name, 0, 0, 0)

    client.putObject(imgBucket, items.code, picture)

    if(request.cookies.get("name").get.value.equals("manager") && request.cookies.get("password").get.value.equals("manager")) {
      DB.save(stocks)
      DB.save(modItem)
      Redirect(routes.HomeController.dashBoard())
    }else{Redirect(routes.HomeController.index())}
  }

  def deleteItem = Action{ implicit request =>
    if(request.cookies.get("name").get.value.equals("manager") && request.cookies.get("password").get.value.equals("manager")) {
      val search = searchForm.bindFromRequest.get
      val deleteItem = (for(n <- DB.query[Items].fetch() if n.name.contains(search)){DB.delete(n)})
      val deleteStock = (for(n <- DB.query[Stocks].fetch() if n.name.contains(search)){DB.delete(n)})
      Redirect(routes.HomeController.dashBoard())
    }else{Redirect(routes.HomeController.index())}
  }

  def checkBox = Action{ implicit request =>
    val a = one.bindFromRequest.get

    val b = (for(n <- 0 to a.length-1)yield{DB.query[Order].fetch().apply(a(n))}).toList
    val c = for(n <- b)yield{

      for(n <- n.item.apply(0)){
        DB.query[Stocks].whereEqual("name", n._1).fetch().map(item => item.copy(quantity = item.quantity - n._2, sold = item.sold + n._2)).map(DB.save)
      }
      //DB.save(DB.query[Stocks].whereEqual("name", n.name).fetch().map(item => item.quantity))
      val sa = Orders.apply(n.name, n.address, n.pDay, n.item, n.price)
      DB.delete(n)
      DB.save(sa)
    }
    Ok(views.html.dashboard())
  }

  def checkBox2 = Action{ implicit request =>
    val a = one.bindFromRequest.get
    println(a)
    val b = (for(n <- 0 until a.length)yield{DB.query[Orders].fetch().apply(a(n))}).toList
    val c = for(n <- b)yield{
      DB.delete(n)
    }
    Ok(views.html.dashboard())
  }

  def checkBox3 = Action{ implicit request =>
    val a = one.bindFromRequest.get

    val dItems = (for(n <- 0 to a.length-1)yield{DB.query[Items].fetch().apply(a(n))}).toList
    val dStocks = for(n <- dItems)yield {n.name}
    val stocks = for(n <- DB.query[Stocks].fetch())yield{}
    /*
    val c = for(n <- b)yield{
      val m = Map("name" -> n.name, "code" -> n.code, "price" -> n.price, "description" -> n.description)
      val sa = Items.apply(n.name, n.code, n.price, n.description)
      DB.delete(n)
    }
    */
    Ok(views.html.dashboard())
  }

  def deleteAllOrder = Action{ implicit request =>
    if(request.cookies.get("name").get.value.equals("manager") && request.cookies.get("password").get.value.equals("manager")) {
      val delete = DB.query[Order].fetch.foreach(a => DB.delete(a))
      val delete2 = DB.query[Orders].fetch.foreach(a => DB.delete(a))

      Redirect(routes.HomeController.index())
    }else{Redirect(routes.HomeController.dashBoard())}
  }

  def sEdit = Action{ implicit request =>
    val values = stockEdit.bindFromRequest.get
    val quantity = values._1
    val returned = values._2
    val id = values._3

    println(values)
    val b = for(n <- DB.query[Stocks].fetch() if values._3.contains(n.id))yield{n}
    println(b)
    val c = for(n <- b)yield{
      /*
      println(n)
      DB.query[Stocks].whereEqual("id", values._3(n)).fetch.map(item => item.copy(quantity = item.quantity + values._1(n), returned = item.returned + values._2(n))).map(DB.save)
      //DB.save(DB.query[Stocks].whereEqual("name", n.name).fetch().map(item => item.quantity))
      */
      val in = id.indexOf(n.id)
      DB.save(n.copy(quantity = n.quantity + quantity(in), returned = n.returned + returned(in)))

    }
    Ok(views.html.dashboard())
  }


}
