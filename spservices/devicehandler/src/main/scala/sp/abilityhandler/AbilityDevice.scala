package sp.abilityhandler

import java.util.UUID

import sp.domain._
import sp.domain.Logic._
import sp.messages._
import Pickles._
import akka.actor._
import akka.persistence._


package APIAbilityHandler {
  sealed trait Request

  case class StartAbility(id: ID, params: Map[ID, SPValue], attributes: SPAttributes) extends Request
  case class ForceResetAbility(id: ID) extends Request
  case class ForceResetAllAbilities() extends Request

  // to be used when handshake is on
  case class ExecuteCmd(cmd: ID) extends Request

  case class GetAbilities() extends Request
  case class SetUpAbility(ability: Ability, handshake: Boolean = false) extends Request


  sealed trait Response

  case class CmdID(cmd: ID) extends Response
  case class AbilityStarted(id: ID) extends Response
  case class AbilityCompleted(id: ID, result: Map[ID, SPValue]) extends Response
  case class AbilityState(state: Map[ID, SPValue]) extends Response

  case class Ability(name: String,
                     id: ID,
                     preCondition: PropositionCondition = PropositionCondition(AlwaysFalse, List()),
                     started: PropositionCondition = PropositionCondition(AlwaysFalse, List()),
                     postCondition: PropositionCondition = PropositionCondition(AlwaysTrue, List()),
                     resetCondition: PropositionCondition = PropositionCondition(AlwaysTrue, List()),
                     parameters: List[ID] = List(),
                     result: List[ID] = List(),
                     attributes: SPAttributes = SPAttributes())


}
import sp.abilityhandler.{APIAbilityHandler => api}


object AbilityHandler {
  def props(name: String, id: UUID, vd: UUID) = Props(classOf[AbilityHandler], name, id, vd)
}




// This actor will keep track of the abilities and parse all messages from the VD
class AbilityHandler(name: String, id: UUID, vd: UUID) extends PersistentActor with ActorLogging with AbilityLogic {
  override def persistenceId = id.toString
  case class AbilityStorage(ability: api.Ability, actor: ActorRef, ids: Set[ID] = Set(), current: Option[AbilityStateChange] = None)

  var abilities: Map[ID, AbilityStorage] = Map()
  var state: Map[ID, SPValue] = Map()

  import akka.cluster.pubsub.DistributedPubSub
  import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Put, Subscribe}
  import context.dispatcher
  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe("services", self)
  mediator ! Subscribe("spevents", self)
  mediator ! Subscribe("events", self)



  // Talk to VD and get all resources and IDs
  // implement heartbeat to check if vd is active (also to handle if vd is started after this actor)

  override def receiveCommand = {
    case x: String =>
      val mess = SPMessage.fromJson(x)

      val res = for {
        m <- mess
        h <- m.getHeaderAs[SPHeader] if h.to == id.toString
        b <- m.getBodyAs[api.Request]
      } yield {

        // Message was to me so i send an SPACK
        mediator ! Publish("answers", m.make(h.copy(replyFrom = id.toString, replyID = Some(ID.newID)), APISP.SPACK()))

        b match {
          case api.StartAbility(id, params, attr) =>
          case api.ForceResetAbility(id) =>
          case x: api.ForceResetAllAbilities =>
          case api.ExecuteCmd(cmd) =>
          case x: api.GetAbilities =>
          case api.SetUpAbility(ab, hand) =>
            val ids = idsFromAbility(ab)
            val act = context.actorOf(AbilityActor.props(ab))
            abilities += ab.id -> AbilityStorage(ab, act, ids)
            act ! NewState(filterState(ids, state))
            mediator ! Publish("answers", m.make(h.copy(replyFrom = id.toString, replyID = Some(ID.newID)), APISP.SPDone()))
        }
      }
  }

  override def receiveRecover = {
    case RecoveryCompleted =>
    case x =>
  }

  def filterState(ids: Set[ID], state: Map[ID, SPValue]) = state.filter(kv => ids.contains(kv._1))

}











// Internal api between abilities and handler
case class StartAbility(state: Map[ID, SPValue], reqID: ID, params: Map[ID, SPValue], attributes: SPAttributes = SPAttributes())
case class ResetAbility(state: Map[ID, SPValue])
case object GetIds
case class NewState(s: Map[ID, SPValue])
case object UnAvailable

case class AbilityIds(ability: ID, ids: Set[ID])
case class CanNotStart(reqID: ID, ability: ID, error: String)
case class AbilityStateChange(ability: ID, state: String, cnt: Long, reqID: Option[ID])
case class StateUpdReq(ability: ID, state: Map[ID, SPValue])


object AbilityActor {
  def props(ability: api.Ability) =
    Props(classOf[AbilityActor], ability)
}

class AbilityActor(val ability: api.Ability) extends Actor with AbilityActorLogic{
  var reqID: Option[ID] = None

  override def receive = {
    case GetIds => sender() ! AbilityIds(ability.id, ids)
    case UnAvailable =>
      makeUnavailable()
      sendAbilityState(sender())

    case StartAbility(s, id, p, attr) =>
      val res = start(s)
      res.foreach { updS =>
        reqID = Some(id)
        if (p.nonEmpty) sender() ! StateUpdReq(ability.id, p)
        sender() ! StateUpdReq(ability.id, updS)
        sendAbilityState(sender())
        "fix timeout here if needed"
      }
      if (res.isEmpty)
        sender() ! CanNotStart(id, ability.id, createNotStartingErrorMessage())

    case ResetAbility(s) =>
      val res = reset(s)
      res.foreach { updS =>
        sender() ! StateUpdReq(ability.id, updS)
      }
      sendAbilityState(sender())

    case NewState(s) =>
      val res = evalState(s)
      res._1.foreach { updS =>
        sendAbilityState(sender())
      }
      res._2.foreach{ updS =>
        sender() ! StateUpdReq(ability.id, updS)
      }
  }

  def sendAbilityState(to: ActorRef) =
    to ! AbilityStateChange(ability.id, state, count, reqID)



}









// The various states that an ability can be in
object AbilityState {
  val unavailable = "unavailable"
  val notEnabled = "notEnabled"
  val enabled = "enabled"
  val starting = "starting"
  val executing = "executing"
  val finished = "finished"
  val forcedReset = "forcedReset"
}

trait AbilityActorLogic extends AbilityLogic{
  val ability: api.Ability
  lazy val ids = idsFromAbility(ability)

  import AbilityState._

  var state: String = unavailable
  var count = 0
  var currentCaller = SPAttributes()


  def makeUnavailable() = state = unavailable
  def makeAvailable() = state = notEnabled

  def start(s: Map[ID, SPValue]) = {
    val tH = evalState(s, starting)
    if (state == starting){
      tH._2
    } else None
  }

  def reset(s: Map[ID, SPValue]) = {
    val tH = evalState(s, forcedReset)
    tH._2
  }


  def evalState(s: Map[ID, SPValue], force: String = "") = {
    val theState = State(s)
    val aS = if (force.isEmpty) state else force
    val abilityState = updateState(aS, theState)

    val newAState = if (state != abilityState._1) Some(abilityState._1) else None
    val newRState = if (theState != abilityState._2) Some(abilityState._2.state) else None

    newAState.foreach(x => state = x)
    (newAState, newRState)
  }

  def updateState(s: String, theState: State): (String, State) = s match {
    case "starting" if ability.preCondition.eval(theState) && state != starting => (starting, ability.preCondition.next(theState))
    case "forcedReset" if state != forcedReset=> (forcedReset, ability.resetCondition.next(theState))
    case "forcedReset" => (checkEnabled(theState), ability.resetCondition.next(theState))
    case "executing" if ability.postCondition.eval(theState) => (finished, ability.postCondition.next(theState))
    case x if ability.started.eval(theState) && x != executing =>
      count += 1
      (executing, ability.started.next(theState))
    case "finished" if ability.resetCondition.eval(theState) => (checkEnabled(theState), ability.resetCondition.next(theState))
    case "unavailable" => (checkEnabled(theState), theState)
    case x if ability.preCondition.eval(theState) && state != starting => (enabled, theState)
    case x => (state, theState)
  }

  def checkEnabled(tS: State) = if (ability.preCondition.eval(tS)) enabled else notEnabled



  def createNotStartingErrorMessage() = {
    s"state: $state"
  }

}

trait AbilityLogic {
  def idsFromAbility(a: api.Ability) = {
    Set(a.preCondition,
      a.postCondition,
      a.started, a.resetCondition).flatMap(extractVariables) ++
      a.parameters ++ a.result
  }

  def extractVariables(p: PropositionCondition) = {
    fromGuard(p.guard) ++ fromAction(p.action)
  }

  def fromGuard(p: Proposition): List[ID] = {
    p match {
      case AND(xs) => xs.flatMap(fromGuard)
      case OR(xs) => xs.flatMap(fromGuard)
      case NOT(x) => fromGuard(x)
      case pe: PropositionEvaluator =>
        val xs = List(pe.left, pe.right)
        xs.collect{
          case SVIDEval(id) => id

        }
      case x => List()
    }
  }


  def fromAction(a: List[Action]): List[ID] = {
    a.map(_.id) ++  a.map(_.value).collect{
      case ASSIGN(id) => id
    }
  }
}
