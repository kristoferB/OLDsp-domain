package sp.triagediagramservice

import sp.domain._
import sp.domain.Logic._
import sp.messages._
import sp.messages.Pickles._

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import scala.util.{ Try, Success, Failure }

import sp.triagediagramservice.{API_PatientEvent => api}

class TriageDiagramDevice extends Actor with ActorLogging {
  import akka.cluster.pubsub._
  import DistributedPubSubMediator.{ Subscribe, Publish }
  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("services", self)
  mediator ! Subscribe("spevents", self)
  mediator ! Subscribe("patient-event-topic", self)

  case class Patient(careContactId: String, patientData: Map[String, String], events: List[Map[String, String]])
  case class Triage(colorMap: Map[String, Int])

  var activePatientCards: Map[String, Patient] = Map()
  var triageCounter: Triage = Triage(Map(
      "Odefinierade" -> 0,
      "Grön" -> 0,
      "Gul" -> 0,
      "Orange" -> 0,
      "Röd" -> 0
      )
    )

  def receive = {
    case mess @ _ if {log.debug(s"TriageDiagramService MESSAGE: $mess from $sender"); false} => Unit

    case x: String => handleRequests(x)

  }

  def isValidTriage(triage: String): Boolean = {
    if (triage == "Grön" || triage == "Gul" || triage == "Orange" || triage == "Röd") {
      return true
    }
    return false
  }

  def getTriageApi(triage: String, toAdd: Boolean): api.TriageEvent = {
    triage match {
      case "Grön" => api.Green(toAdd)
      case "Gul" => api.Yellow(toAdd)
      case "Orange" => api.Orange(toAdd)
      case "Röd" => api.Red(toAdd)
      case _ => api.Undefined(toAdd)
    }
  }

  def handleRequests(x: String): Unit = {
    val mess = SPMessage.fromJson(x)
    matchRequests(mess)
  }

  def matchRequests(mess: Try[SPMessage]) = {
    val header = SPHeader(from = "triageDiagramService")
    TriageDiagramComm.extractPatientEvent(mess) map { case (h, b) =>
      var extractedTriageMap = triageCounter.colorMap
      b match {
        case api.NewPatient(careContactId, patientData, events) => {
          println("+ New patient: " + careContactId + ", Prio: " + patientData("Priority"))
          //if (isValidTriage(triage)) {
            //extractedTriageMap += triage -> (extractedTriageMap(triage) + 1)
            //triageCounter = Triage(extractedTriageMap)
          //} else {
            //extractedTriageMap += "Odefinierade" -> (extractedTriageMap("Odefinierade") + 1)
            //triageCounter = Triage(extractedTriageMap)
          //}
          activePatientCards += careContactId -> Patient(careContactId, patientData, events)
          publishOnAkka(header, getTriageApi(patientData("Priority"), true))
        }
        case api.DiffPatient(careContactId, patientDataDiff, newEvents, removedEvents) => {
          println("Diff patient: " + careContactId + ", Prio: " + patientDataDiff("Priority"))
          var modPatientData = activePatientCards(careContactId).patientData
          val oldTriage = modPatientData("Priority")
          patientDataDiff.foreach{ d =>
            modPatientData += d._1 -> d._2
          }
          val newTriage = modPatientData("Priority")
          activePatientCards += careContactId -> Patient(careContactId, modPatientData, activePatientCards(careContactId).events)
          publishOnAkka(header, getTriageApi(oldTriage, false))
          publishOnAkka(header, getTriageApi(newTriage, true))

          //if (isValidTriage(newTriage)) {
          //  if (isValidTriage(oldTriage)) {
          //    if (newTriage != oldTriage) {
                //extractedTriageMap += newTriage -> (extractedTriageMap(newTriage) + 1)
                //extractedTriageMap += oldTriage -> (extractedTriageMap(oldTriage) - 1)
                //triageCounter = Triage(extractedTriageMap)
          //    }
          //  } else {
              //extractedTriageMap += newTriage -> (extractedTriageMap(newTriage) + 1)
              //extractedTriageMap += "Odefinierade" -> (extractedTriageMap("Odefinierade") - 1)
              //triageCounter = Triage(extractedTriageMap)
          //  }
          //} else {
            //extractedTriageMap += "Odefinierade" -> (extractedTriageMap("Odefinierade") + 1)
            //triageCounter = Triage(extractedTriageMap)
          //}
        }
        case api.RemovedPatient(careContactId) => {
          println("- Removed patient: " + careContactId)
          activePatientCards -= careContactId
          publishOnAkka(header, getTriageApi(activePatientCards(careContactId).patientData("Priority"), false))
          //if (isValidTriage(triage)) {
            //extractedTriageMap += triage -> (extractedTriageMap(triage) - 1)
            //triageCounter = Triage(extractedTriageMap)
          //} else {
            //extractedTriageMap += "Odefinierade" -> (extractedTriageMap("Odefinierade") - 1)
            //triageCounter = Triage(extractedTriageMap)
        //  }
        }
      }
    }
  }

  def publishOnAkka(header: SPHeader, body: api.TriageEvent) {
    val toSend = TriageDiagramComm.makeMess(header, body)
    toSend match {
      case Success(v) =>
        mediator ! Publish("triage-diagram-widget-topic", v) // Publishes on bus for widget to receive
      case Failure(e) =>
        println("Failed")
    }
  }

  }

  object TriageDiagramDevice {
    def props = Props(classOf[TriageDiagramDevice])
  }
