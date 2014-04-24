package controllers
package actors

import akka.util.Duration
import play.api.mvc.{AnyContent, Request}
import util.concurrent.BackgroundProcess
import util.plugins.SoftLayer
import models.Asset

case class ActivationProcessor(asset: Asset, userTimeout: Option[Duration] = None)(implicit req: Request[AnyContent]) extends BackgroundProcess[Boolean] {
  override def defaultTimeout: Duration = Duration.parse("60 seconds")
  val timeout = userTimeout.getOrElse(defaultTimeout)

  def run(): Boolean = {
    // iterate over activation plugins, and figure out which one is applicable for this asset
    // then activate the server with that plugin
    SoftLayer.pluginEnabled match {
      case Some(p) => p.softLayerId(asset) match {
        case Some(id) => p.activateServer(id)()
        case _        => false
      }
      case _ => {
        //TODO add other activation plugins here
        false
      }
    }
  }
}

