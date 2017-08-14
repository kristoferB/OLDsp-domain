package spgui.widgets.abilityhandler

import java.util.UUID

import julienrf.json.derived.TypeTagOWrites
import sp.domain._
import sp.domain.Logic._

object APIVirtualDevice {
  sealed trait Request
  val service = "VirtualDevice"

  // requests setup
  case class SetUpDeviceDriver(driver: Driver) extends Request
  case class SetUpResource(resource: Resource) extends Request
  case class GetResources() extends Request

  sealed trait DriverStateMapper
  case class OneToOneMapper(thing: ID, driverID: ID, driverIdentifier: String) extends DriverStateMapper

  // requests command (gets a SPACK and when applied, SPDone (and indirectly a StateEvent))
  case class ResourceCommand(resource: ID, stateRequest: Map[ID, SPValue], timeout: Int = 0) extends Request

  // requests from driver
  case class DriverStateChange(name: String, id: ID, state: Map[String, SPValue], diff: Boolean = false) extends Request
  case class DriverCommand(name: String, id: ID, state: Map[String, SPValue]) extends Request
  case class DriverCommandDone(requestID: ID, result: Boolean) extends Request

  // answers
  sealed trait Response
  case class StateEvent(resource: String, id: ID, state: Map[ID, SPValue], diff: Boolean = false) extends Response

  case class Resources(xs: List[Resource]) extends Response
  case class Drivers(xs: List[Driver]) extends Response
  case class NewResource(x: Resource) extends Response
  case class RemovedResource(x: Resource) extends Response
  case class NewDriver(x: Driver) extends Response
  case class RemovedDriver(x: Driver) extends Response

  case class Resource(name: String, id: ID, things: Set[ID], stateMap: List[DriverStateMapper], setup: SPAttributes, sendOnlyDiffs: Boolean = false)
  case class Driver(name: String, id: ID, driverType: String, setup: SPAttributes)

  import play.api.libs.json._

  implicit lazy val fDriver: JSFormat[Driver] = Json.format[Driver]
  implicit lazy val fResource: JSFormat[Resource] = Json.format[Resource]

  implicit lazy val fSetUpDeviceDriver: JSFormat[SetUpDeviceDriver] = Json.format[SetUpDeviceDriver]
  implicit lazy val fSetUpResource:     JSFormat[SetUpResource]     = Json.format[SetUpResource]
  implicit lazy val fGetResources:     JSFormat[GetResources]     = deriveFormatISA[GetResources]
  implicit lazy val fOneToOneMapper:     JSFormat[OneToOneMapper]     = Json.format[OneToOneMapper]
  implicit lazy val fResourceCommand:     JSFormat[ResourceCommand]     = Json.format[ResourceCommand]
  implicit lazy val fDriverStateChange:     JSFormat[DriverStateChange]     = Json.format[DriverStateChange]
  implicit lazy val fDriverCommand:     JSFormat[DriverCommand]     = Json.format[DriverCommand]
  implicit lazy val fDriverCommandDone:     JSFormat[DriverCommandDone]     = Json.format[DriverCommandDone]
  implicit lazy val fStateEvent:     JSFormat[StateEvent]     = Json.format[StateEvent]
  implicit lazy val fResources:     JSFormat[Resources]     = Json.format[Resources]
  implicit lazy val fDrivers:     JSFormat[Drivers]     = Json.format[Drivers]
  implicit lazy val fNewResource:     JSFormat[NewResource]     = Json.format[NewResource]
  implicit lazy val fRemovedResource:     JSFormat[RemovedResource]     = Json.format[RemovedResource]
  implicit lazy val fNewDriver:     JSFormat[NewDriver]     = Json.format[NewDriver]
  implicit lazy val fRemovedDriver:     JSFormat[RemovedDriver]     = Json.format[RemovedDriver]
  

  object Request {
    implicit lazy val fVirtualDeviceRequest: JSFormat[Request] = Json.format[Request]
  }
  object Response {
    implicit lazy val fVirtualDeviceResponse: JSFormat[Response] = Json.format[Response]
  }
  object DriverStateMapper {
    implicit lazy val fDriverStateMapper: JSFormat[DriverStateMapper] = deriveFormatISA[DriverStateMapper]
  }

}

object APIAbilityHandler {
  sealed trait Request
  sealed trait Response
  val service = "abilityHandler"

  final case class StartAbility(id: ID, params: Map[ID, SPValue] = Map(), attributes: SPAttributes = SPAttributes()) extends Request
  final case class ForceResetAbility(id: ID) extends Request
  case object ForceResetAllAbilities extends Request

  // to be used when handshake is on
  final case class ExecuteCmd(cmd: ID) extends Request

  case object GetAbilities extends Request
  final case class SetUpAbility(ability: Ability, handshake: Boolean = false) extends Request



  final case class CmdID(cmd: ID) extends Response
  final case class AbilityStarted(id: ID) extends Response
  final case class AbilityCompleted(id: ID, result: Map[ID, SPValue]) extends Response
  final case class AbilityState(id: ID, state: Map[ID, SPValue]) extends Response
  final case class Abilities(xs: List[Ability]) extends Response
  final case class Abs(a: List[(ID,String)]) extends Response

  final case class Ability(name: String,
                     id: ID,
                     preCondition: Condition = Condition(AlwaysFalse, List()),
                     started: Condition = Condition(AlwaysFalse, List()),
                     postCondition: Condition = Condition(AlwaysTrue, List()),
                     resetCondition: Condition = Condition(AlwaysTrue, List()),
                     parameters: List[ID] = List(),
                     result: List[ID] = List(),
                     attributes: SPAttributes = SPAttributes())



  import play.api.libs.json._
  import play.api.libs.json.Reads._


  implicit lazy val fAbility: JSFormat[Ability] = Json.format[Ability]
  // handcrafting json due to too large macro-generation in scalajs
  object Response {
    val fCmdID: JSFormat[CmdID] = Json.format[CmdID]
    val fAbilityStarted: JSFormat[AbilityStarted] = Json.format[AbilityStarted]
    val fAbilityCompleted: JSFormat[AbilityCompleted] = Json.format[AbilityCompleted]
    val fAbilityState: JSFormat[AbilityState] = Json.format[AbilityState]
    val fAbilities: JSFormat[Abilities] = Json.format[Abilities]
    val fAbs: JSFormat[Abs] = Json.format[Abs]
    implicit lazy val fExampleServiceRequest: JSFormat[Response] = new JSFormat[Response] {

      override def reads(json: SPValue): JsResult[Response] = {
        (JsPath \ "isa").read[String].reads(json).flatMap{
          case "CmdID" => json.validate[CmdID](fCmdID)
          case "AbilityStarted" =>json.validate[AbilityStarted](fAbilityStarted)
          case "AbilityCompleted" => json.validate[AbilityCompleted](fAbilityCompleted)
          case "AbilityState" => json.validate[AbilityState](fAbilityState)
          case "Abilities" => json.validate[Abilities](fAbilities)
          case "Abs" => json.validate[Abs](fAbs)
        }
      }

      override def writes(o: Response): SPValue = {
        o match {
          case x: CmdID => SPValue(x)(fCmdID)
          case x: AbilityStarted => SPValue(x)(fAbilityStarted)
          case x: AbilityCompleted => SPValue(x)(fAbilityCompleted)
          case x: AbilityState => SPValue(x)(fAbilityState)
          case x: Abilities => SPValue(x)(fAbilities)
          case x: Abs => SPValue(x)(fAbs)
        }
      }
    }
  }


  object Request {
    val fStartAbility: JSFormat[StartAbility] = Json.format[StartAbility]
    val fSetUpAbility: JSFormat[SetUpAbility] = Json.format[SetUpAbility]
    val fForceResetAbility: JSFormat[ForceResetAbility] = Json.format[ForceResetAbility]
    val fExecuteCmd: JSFormat[ExecuteCmd] = Json.format[ExecuteCmd]
    implicit lazy val fExampleServiceRequest: JSFormat[Request] = new JSFormat[Request] {

      override def reads(json: SPValue): JsResult[Request] = {
        (JsPath \ "isa").read[String].reads(json).flatMap{
          case "StartAbility" => json.validate[StartAbility](fStartAbility)
          case "ForceResetAbility" =>json.validate[ForceResetAbility](fForceResetAbility)
          case "ForceResetAllAbilities " =>JsSuccess(ForceResetAllAbilities)
          case "ExecuteCmd" => json.validate[SetUpAbility](fSetUpAbility)
          case "GetAbilities" => JsSuccess(GetAbilities)
          case "SetUpAbility" => json.validate[ExecuteCmd](fExecuteCmd)
        }
      }

      override def writes(o: Request): SPValue = {
        o match {
          case x: StartAbility => SPValue(x)(fStartAbility)
          case x: ForceResetAbility => SPValue(x)(fForceResetAbility)
          case ForceResetAllAbilities => SPAttributes("isa"-> "ForceResetAllAbilities")
          case x: ExecuteCmd => SPValue(x)(fExecuteCmd)
          case GetAbilities => SPAttributes("isa"-> "GetAbilities")
          case x: SetUpAbility => SPValue(x)(fSetUpAbility)
        }
      }
    }
  }
}