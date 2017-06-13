package spgui.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.vdom.all.aria

object SPDropdownNew {
  case class Props(buttonTags: Seq[TagMod], contents: List[ReactNode])

  private val component = ReactComponentB[Props]("SPButton")
    .render_P(p =>
      <.li(
        p.buttonTags,
          ReactAttr.Generic("data-toggle") := "dropdown",
        <.div(
          ^.id:="something",
          ^.className := "nav-link dropdown-toggle",
          aria.haspopup := "true",
          aria.expanded := "false"
        ),
        <.ul(
          p.contents,
          ^.className := ComponentCSS.dropDownList.htmlClass,
          ^.className := "dropdown-menu",
          aria.labelledby := "something"
        )
      )).build

  def apply(buttonTags: Seq[TagMod], contents: List[ReactNode]) =
    component(Props(buttonTags, contents))
}

