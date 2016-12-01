package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import sorm._
import models._

/**
  * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

class HomeController extends Controller {

  val products = Redirect(routes.HomeController.list(0, 2, ""))

  val userForm: Form[User] = Form{
    mapping(
      "name" -> text,
      "password" -> text
    )(User.apply)(User.unapply)
  }


  def index = Action {
    Ok(views.html.index())
  }

  def login = Action{ implicit request =>
    Ok(views.html.login())
  }

  def productsPage = Action{products}


  def dashBoard = Action{ implicit request =>
    Ok(views.html.dashboard())
  }

  def userAuth = Action{ implicit request =>
    val user = userForm.bindFromRequest.get
    if(user.name.equals("manager") && user.password.equals("manager")){
      Redirect(routes.HomeController.dashBoard()).withCookies(Cookie("name", user.name, Option(3600))).withCookies(Cookie("password", user.password, Option(3600)))
    }else{Redirect(routes.HomeController.index())}
  }

  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(views.html.products(
      Pagination.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  def logOut = Action{ implicit request =>
    Ok(views.html.index()).withCookies().discardingCookies(DiscardingCookie("name")).discardingCookies(DiscardingCookie("password"))
  }

}
