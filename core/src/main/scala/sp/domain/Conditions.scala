package sp.domain

/**
 * Created by Kristofer on 2014-06-10.
 */
trait Condition {
  val attributes: SPAttributes
}


case class Action(stateVariableID: ID, value: StateUpdater)
case class PropositionCondition(guard: Proposition,
                                action: List[Action],
                                attributes: SPAttributes = SPAttributes(Map())) extends Condition {
}





// propositional logic conditions

sealed trait Proposition


case class AND(props: List[Proposition]) extends Proposition {
  override def toString = StrMaker.makeStr(props, "&&")
}
case class OR(props: List[Proposition]) extends Proposition{
  override def toString = StrMaker.makeStr(props, "||")
}
case class NOT(p: Proposition) extends Proposition {
  override def toString = s"!$p"
}

case class EQ(left: StateEvaluator, right: StateEvaluator) extends Proposition {
  override def toString = s"$left == $right "
}
case class NEQ(left: StateEvaluator, right: StateEvaluator) extends Proposition {
  override def toString = s"$left != $right "
}

trait StateEvaluator

case class SVIDEval(id: ID) extends StateEvaluator {
  override def toString = id.toString
}
case class SVNameEval(v: String) extends StateEvaluator {
  override def toString = v
}
case class ValueHolder(v: SPAttributeValue) extends StateEvaluator with StateUpdater {
  override def toString = v.toString
}

//TODO: add StateEvaluator for a+b, a+1 etc when nedded 140630



sealed trait StateUpdater

case class INCR(n: Int) extends StateUpdater
case class DECR(n: Int) extends StateUpdater
case class ASSIGN(id: ID) extends StateUpdater

private object StrMaker {
  def makeStr[T](xs: List[T], div: String) = {
    def req(list: List[T]): String = list match {
      case x :: Nil => x.toString
      case x :: xs => s"$x $div ${req(xs)}"
    }
    if (xs.isEmpty) s"$div EMPTY"
    else s"(${req(xs)})"
  }
}
