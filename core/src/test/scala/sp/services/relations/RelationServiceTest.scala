package sp.services.relations

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest._
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import com.typesafe.config._
import sp.domain._
import sp.services.specificationconverters.ConditionsFromSpecs
import sp.system.messages._

/**
 * Created by Kristofer on 2014-06-17.
 */
class RelationServiceTest extends TestKit(ActorSystem("test")) with FreeSpecLike with ImplicitSender
  with Matchers with BeforeAndAfterAll with TestThings {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "The relation service" - {

    val mh = system.actorOf(Props(classOf[TestModel], List(o1,o2), ModelInfo("test", 1, Attr()), List(v1,v2,v3), List(), false))
    val sh = mh
    val rls = system.actorOf(RelationService.props(mh, sh, "condService"), "rlsTest")

    "find rels " in {
      import Attribs._
      val attr = Attr("model"->"test", "operations"->ListPrimitive(List(o1.id, o2.id))).addStateAttr("initstate", state)
      rls ! Request("RelationFinder", attr)
      expectMsg("")
    }
  }
}

trait TestThings {
  val range = MapPrimitive(Map("start" -> SPAttributeValue(0), "end" -> SPAttributeValue(3), "step" -> SPAttributeValue(1)))
  val domain = ListPrimitive(List(StringPrimitive("hej"), StringPrimitive("då")))
  val attrD = SPAttributes(Map("domain" -> domain))
  val attrR = SPAttributes(Map("range" -> range))
  val attrB = SPAttributes(Map("boolean" -> true))

  val v1 = StateVariable("v1", attrR)
  val v2 = StateVariable("v2", attrD)
  val v3 = StateVariable("v3", attrB)


  val eq = EQ(SVIDEval(v1.id), ValueHolder(SPAttributeValue(0)))
  val neq = NEQ(SVIDEval(v2.id), ValueHolder(SPAttributeValue("då")))

  val o1Cond = PropositionCondition(
    EQ(SVIDEval(v1.id), ValueHolder(SPAttributeValue(0))),
    List(Action(v1.id, ValueHolder(1))))
  val o2Cond = PropositionCondition(
    EQ(SVIDEval(v1.id), ValueHolder(SPAttributeValue(1))),
    List(Action(v1.id, ValueHolder(2))))
  val noActionCond = PropositionCondition(
    EQ(SVIDEval(v1.id), ValueHolder(SPAttributeValue(0))),
    List())

  val o1 = Operation("o1", List(o1Cond))
  val o2 = Operation("o2", List(o2Cond))

  val state = State(Map(v1.id -> 0, v2.id -> "hej", v3.id -> false, o1.id -> "i", o2.id -> "i"))
  val state2 = State(Map(v1.id -> 2, v2.id -> "då", v3.id -> false, o1.id -> "i", o2.id -> "i"))

  val vm: Map[ID, StateVariable] =
    Map(v1.id -> v1, v2.id -> v2, v3.id -> v3, o1.id -> o1, o2.id -> o2)
}


class TestModel(ops: List[Operation],
                modelInfo: ModelInfo,
                stateVars: List[StateVariable],
                specs: List[SOPSpec],
                fail: Boolean) extends Actor {

  def receive = {
    case x: GetIds => sender ! SPIDs(ops)
    case x: GetModelInfo => sender ! modelInfo
    case x: GetStateVariables => sender ! SPSVs(stateVars)
    case x: GetResults => sender ! SPIDs(specs)
    case x: Request => sender ! ConditionsFromSpecs(Map())
    case x @ _ => println("TestModel got: " + x)
  }
}
