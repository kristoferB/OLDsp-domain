import com.typesafe.sbt.SbtPgp.autoImportImpl.useGpg
import sbt.{Def, Developer, ScmInfo, url}
import sbt.Keys._

import SPSettings._

object PublishingSettings {

  object infoPub {
    // Name
    lazy val organizationName = "sequenceplanner"
    lazy val comString = "com"
    lazy val githubString = "github"

    lazy val orgNameFull = comString +"."+ githubString +"."+ organizationName
    lazy val groupIdSonatype = comString +"."+ githubString +"."+ organizationName

    lazy val projectName = "sp-domain"

    // Version
    lazy val spDomainVersion = "0.0.1"

    // General Pub INFO
    lazy val mitLicense = Seq("MIT License" -> url("https://opensource.org/licenses/MIT"))

    lazy val githubSP = Some(url("https://github.com/sequenceplanner"))

    lazy val devAlexander = Developer(
      id    = "aleastChs",
      name  = "Alexander Åstrand",
      email = "aleast@student.chalmers.se",
      url   = url("https://github.com/aleastChs")
    )

    lazy val scmSPDomain = Some(
      ScmInfo(
        url("https://github.com/sequenceplanner/sp-domain"),
        "scm:git@github.com:sequenceplanner/sp-domain.git"
      )
    )
  }

  lazy val publishing = Seq(
    scalaVersion := versions.scala,
    version := infoPub.spDomainVersion,
    organization := infoPub.orgNameFull,
    // use gpg - command line
    useGpg := true
  )

}