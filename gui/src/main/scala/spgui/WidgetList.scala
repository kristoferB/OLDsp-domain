package spgui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._


object WidgetList {

   val erica =
    List[(String, SPWidgetBase => ReactElement, Int, Int)](
      ("Klocka", widgets.ClockWidget(), 2, 2),

      // Team widgets -->
      ("Triagediagram", widgets.TriageWidget(), 2, 8),
      ("Statusdiagram", widgets.StatusWidget(), 2, 3),
      ("Platsdiagram", widgets.PlaceWidget(), 2, 3),
      ("Patientkort", widgets.PatientCardsWidget(), 5, 19),
      ("Felsökning", widgets.DebuggingWidget(), 5, 19),
      ("Lång tid sedan händelse", widgets.PatientReminderWidget(), 1, 19),
      // <--

      // Coordinator widgets -->
      ("Rumskarta (koordinator)", widgets.RoomOverviewWidget(), 4, 15),
      ("Triage- och statusdiagram (koordinator)", widgets.CoordinatorDiagramWidget(), 3, 17),
      ("Väntrumsdiagram (koordinator)", widgets.WaitingRoomWidget(), 3, 4)
      // <--
    )

  val sp =
    List[(String, SPWidgetBase => ReactElement, Int, Int)](
      ("Grid Test", spgui.dashboard.GridTest(), 5, 5),
      ("Widget Injection", widgets.injection.WidgetInjectionTest(), 2, 2),
      ("Item Editor", widgets.itemeditor.ItemEditor(), 2, 2),
      //("DragDrop Example", widgets.examples.DragAndDrop(), 2, 2),
      //("Widget with json", widgets.examples.WidgetWithJSON(), 2, 2),
      ("PlcHldrC", PlaceholderComp(), 2, 2),
      ("SPWBTest", SPWidgetBaseTest(), 2, 2),
      //("Widget with data", widgets.examples.WidgetWithData(), 2, 2),
      ("CommTest", widgets.WidgetCommTest(), 2, 2),
      ("D3Example", widgets.examples.D3Example(), 2, 2),
      ("D3ExampleServiceWidget", widgets.examples.D3ExampleServiceWidget(), 2, 2),
      //("ExampleServiceWidget", ExampleServiceWidget(), 2, 2),
      //("ExampleServiceWidgetState", ExampleServiceWidgetState(), 2, 3),
      ("OpcUAWidget", widgets.examples.OpcUAWidget(), 5, 2),
      ("Item explorer", widgets.itemexplorer.ItemExplorer(), 2, 4),
      ("Ability Handler", widgets.abilityhandler.AbilityHandlerWidget(), 2, 2),
      ("ServiceList", widgets.services.ServiceListWidget(), 2, 2),
      ("Settings", widgets.settings.SettingsWidget(), 2, 4)
    )

   val list = erica //++ sp

  val map = list.map(t => t._1 -> (t._2, t._3, t._4)).toMap
}

object PlaceholderComp {
  val component = ReactComponentB[Unit]("PlaceholderComp")
    .render(_ => <.h2("placeholder"))
    .build

  def apply() = SPWidget(spwb => component())
}


object SectionList {
  // To be loaded from the backend soon!
  val sections = List(
    "gul",
    "blå"
  )
}
