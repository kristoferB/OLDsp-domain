package sp.services.relations

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import sp.domain._
import sp.system.messages._
import sp.services.specificationconverters._

import scala.concurrent.duration._
import scala.util._

/**
 * Created by Kristofer on 2014-08-04.
 */
class RelationService(modelHandler: ActorRef, serviceHandler: ActorRef, conditionsFromSpecService: String) extends Actor {

  import sp.system.SPActorSystem._

  private implicit val to = Timeout(1 seconds)

  import context.dispatcher
  import Attribs._

  def receive = {
    case Request(_, attr) => {
      val reply = sender
      extract(attr) match {
        case Some((model, opsID, init, groups, iterations, goal)) => {

          // Retreive from model
          // todo: Handle this in a more general way soon
          val opsF = modelHandler ? GetIds(opsID, model)
          val modelInfoF = modelHandler ? GetModelInfo(model)
          val svsF = modelHandler ? GetStateVariables(model)
          val currentRelationsF = (modelHandler ? GetResults(model, _.isInstanceOf[RelationResult]))
          val specsCondsF = serviceHandler ? Request(conditionsFromSpecService, Attr("model"-> model))

          //            .mapTo[List[RelationResult]] map (_.sortWith(_.modelVersion > _.modelVersion))

          val resultFuture = for {
            opsAnswer <- opsF
            modelInfoAnswer <- modelInfoF
            stateVarsAnswer <- svsF
            currentRelAnswer <- currentRelationsF
            specsConds <- specsCondsF

          } yield {
            List(opsAnswer, modelInfoAnswer, stateVarsAnswer, currentRelAnswer, specsConds) match {
              case SPIDs(opsIDAble) ::
                ModelInfo(_, mVersion, _) ::
                SPSVs(svsIDAble) ::
                SPIDs(olderRelsIDAble) ::
                ConditionsFromSpecs(condMap) ::
                Nil => {

//                println(s"relationsSerice got:")
//                println(s"ops: $opsIDAble ")
//                println(s"mVersion: $mVersion ")
//                println(s"svsIdAble: $svsIDAble ")
//                println(s"olderRels: $olderRelsIDAble ")
//                println(s"condMap: $condMap ")

                val ops = opsIDAble map (_.asInstanceOf[Operation])
                val svs = svsIDAble map (_.asInstanceOf[StateVariable])
                val olderRels = olderRelsIDAble map (_.asInstanceOf[RelationResult]) sortWith (_.modelVersion > _.modelVersion)

                if (olderRels.nonEmpty && olderRels.head.modelVersion == mVersion) reply ! olderRels.head
                else {

                  val stateVarsMap = svs.map(sv => sv.id -> sv).toMap ++ createOpsStateVars(ops)
                  val goalfunction: State => Boolean = if (goal == None) (s: State) => false else (s: State) => {
                    val g = goal.get
                    g.state.forall{case (sv, value) => s(sv) == value}
                  }

                  val filterCondMap = condMap.map { case (id, conds) =>
                    val filtered = if (groups.isEmpty) conds else conds.filter(c => groups.contains(c.attributes.getAsString("group")))
                    id -> filtered
                  }
                  val updatedOps = ops.map{ o =>
                    val updatedConds = filterCondMap.getOrElse(o.id, List[Condition]()) ++ o.conditions
                    val id = o.id
                    val version = o.version
                    o.copy(conditions = updatedConds).update(id, version).asInstanceOf[Operation]
                  }

                  println(s"updated ops: $updatedOps")

                  println(s"init: $init")
                  println(s"added: ${addOpsToState(updatedOps, init)}")

                  // just one actor per request initially
                  val relationFinder = context.actorOf(RelationFinder.props)
                  val inputToAlgo = FindRelations(updatedOps, stateVarsMap ++ createOpsStateVars(updatedOps), addOpsToState(updatedOps, init), groups, iterations, goalfunction)

                  //TODO: Handle long running algorithms better
                  val answer = relationFinder ? inputToAlgo
                  answer onComplete {
                    case Success(res: RelationMap) => {
                      val relation = RelationResult("RelationMap", res, model, mVersion + 1)
                      reply ! relation
                      modelHandler ! UpdateIDs(model, List(UpdateID(relation.id, -1, relation)))
                    }
                    case Success(res) => println("WHAT IS THIS RELATION FINDER RETURNS: " + res)
                    case Failure(error) => reply ! SPError(error.getMessage)
                  }
                }
              }
              //TODO: This error handling should also be part of the general solution when extracting
              case error @ x :: xs => {
                val respond = error.foldLeft("- ")(_ + "\n- " + _.toString)
                reply ! respond
              }
            }
          }
          resultFuture.recover { case e: Exception => println("Resultfuture Fail: " + e.toString)}
        }
        case None => reply ! errorMessage(attr)
      }
    }
  }



  //TODO: I will fix this in a more general way so we can return errors if something is missing (probably using HList)
  def extract(attr: SPAttributes) = {
    for {
      model <- attr.getAsString("model")
      ops <- attr.getAsList("operations") map( _.flatMap(_.asID))
      initState <- attr.getStateAttr("initstate")
    } yield {
      val groups = attr.getAsList("groups").getOrElse(List())
      val goalState = attr.getStateAttr("goal")
      val iterations = attr.getAsInt("iteration").getOrElse(100)
      (model, ops, initState, groups, iterations, goalState)
    }
  }

  def errorMessage(attr: SPAttributes) = {
    SPError("The request is missing parameters: \n" +
      s"model: ${attr.getAsString("model")}" + "\n" +
      s"ops: ${attr.getAsList("operations") map (_.flatMap(_.asID))}" + "\n" +
      s"initstate: ${attr.getStateAttr("initstate")}" + "\n" +
      s"groups(optional): ${attr.getAsList("groups")}" + "\n" +
      s"goal(optional): ${attr.getStateAttr("goal")}" + "\n" +
      s"iterations(optional): ${attr.getAsInt("iteration")}")
  }

  /**
   * Adds opertion init state ("i") to a state
   * @param ops
   * @param state
   * @return an updated state object
   */
  def addOpsToState(ops: List[Operation], state: State) = {
    state.next(ops.map(_.id -> StringPrimitive("i")).toMap)
  }

  /**
   * Adds opertion statevariables to the stateVarMap
   * @param ops The operations
   * @return a stateVar map
   */
  def createOpsStateVars(ops: List[Operation]) = {
    ops.map(o => o.id -> StateVariable.operationVariable(o)).toMap
  }
}


object RelationService{
  def props(modelHandler: ActorRef, serviceHandler: ActorRef, conditionsFromSpecService: String) = Props(classOf[RelationService], serviceHandler, modelHandler, conditionsFromSpecService)

}