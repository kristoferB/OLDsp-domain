package spgui.widgets.abilityhandler

import java.util.UUID
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spgui.communication._
import sp.domain._
import sp.messages._
import sp.messages.Pickles._

import spgui.widgets.abilityhandler.{APIVirtualDevice => vdapi}
import spgui.widgets.abilityhandler.{APIAbilityHandler => abapi}

object AbilityHandlerWidget {
  case class State(resources: List[vdapi.Resource], abilities: List[abapi.Ability], abilityState: Map[ID, SPValue])

  private class Backend($: BackendScope[Unit, State]) {
    val answerHandler = BackendCommunication.getMessageObserver(
      mess => {
        AbilityComm.extractVDReply(mess).map{
          case vdapi.Resources(r) =>
            $.modState(s => s.copy(resources = r)).runNow()
          case x =>
            println(s"AbilityHandlerWidget - TODO: $x")
        }
        fromSPValue[abapi.Response](mess.body).map{
          case abapi.Abilities(a) =>
            $.modState(s => s.copy(abilities = a)).runNow()
          case abapi.AbilityState(id, state) =>
            $.modState{s =>
              val ns = s.abilityState ++ state
              s.copy(abilityState = ns)}.runNow()
          case x =>
            println(s"AbilityHandlerWidget - answers - TODO: $x")
        }
      },
      "answers"
    )

    val eventHandler = BackendCommunication.getMessageObserver(
      mess => {
        fromSPValue[abapi.Response](mess.body).map{
          case abapi.AbilityState(id, state) =>
            $.modState{s =>
              val ns = s.abilityState ++ state
              s.copy(abilityState = ns)}.runNow()
          case x =>
            println(s"AbilityHandlerWidget - events - TODO: $x")
        }
      },
      "events"
    )

    def render(s: State) = {
      <.div(
        <.h2("Ability Handler"),
        <.br(),
        <.button(
          ^.className := "btn btn-default",
          ^.onClick --> sendToVD(vdapi.GetResources()), "Get resources"
        ),
        <.button(
          ^.className := "btn btn-default",
          ^.onClick --> sendToAB(abapi.GetAbilities()), "Get abilities"
        ),
        renderResources(s),
        renderAbilities(s)
      )
    }

    def renderResources(s: State) = {
      <.table(
        ^.width:="400px",
        <.caption("Resources"),
        <.thead(
          <.tr(
            <.th(^.width:="100px","Name")
          )
        ),
        <.tbody(
          s.resources.map(r=> {
            <.tr(
              <.td(r.name)
            )
          })
        )
      )
    }

    def getAbilityState(s: SPValue): String = {
      s.getAs[String]("state").getOrElse("")
    }

    def getAbilityCount(s: SPValue): Int = {
      s.getAs[Int]("count").getOrElse(0)
    }

    def renderAbilities(s: State) = {
      <.table(
        ^.width:="550px",
        <.caption("Abilties"),
        <.thead(
          <.tr(
            <.th(^.width:="200px","Name"),
            <.th(^.width:="100px","State"),
            <.th(^.width:="50px","Count"),
            <.th(^.width:="100px","Start"),
            <.th(^.width:="100px","Reset")
          )
        ),
        <.tbody(
          s.abilities.sortBy(a=>a.name).map(a=> {
            <.tr(
              <.td(a.name),
              <.td(getAbilityState(s.abilityState.get(a.id).getOrElse(SPValue.empty))),
              <.td(getAbilityCount(s.abilityState.get(a.id).getOrElse(SPValue.empty))),
              <.td(<.button(
                ^.className := "btn btn-sm",
                ^.onClick --> sendToAB(abapi.StartAbility(a.id)), "Start"
              )),
              <.td(<.button(
                ^.className := "btn btn-sm",
                ^.onClick --> sendToAB(abapi.ForceResetAbility(a.id)), "Reset"
              ))
            )
          })
        )
      )
    }

    def onUnmount() = {
      println("Unmounting")
      answerHandler.kill()
      eventHandler.kill()
      Callback.empty
    }

    def sendToVD(mess: vdapi.Requests): Callback = {
      val h = SPHeader(from = "AbilityHandlerWidget", to = vdapi.attributes.service,
        reply = SPValue("AbilityHandlerWidget"), reqID = java.util.UUID.randomUUID())
      val json = SPMessage(*(h), *(mess))
      BackendCommunication.publish(json, "services")
      Callback.empty
    }

    def sendToAB(mess: abapi.Request): Callback = {
      val h = SPHeader(from = "AbilityHandlerWidget", to = abapi.attributes.service,
        reply = SPValue("AbilityHandlerWidget"), reqID = java.util.UUID.randomUUID())
      val json = SPMessage(*(h), *(mess))
      BackendCommunication.publish(json, "services")
      Callback.empty
    }
  }

  private val component = ReactComponentB[Unit]("AbilityHandlerWidget")
    .initialState(State(resources = List(), abilities = List(), abilityState = Map()))
    .renderBackend[Backend]
    .componentWillUnmount(_.backend.onUnmount())
    .build

  def apply() = spgui.SPWidget(spwb => component())
}
