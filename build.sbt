import SPSettings._
import PublishingSettings._
import sbt.Keys.{pomExtra, pomIncludeRepository}

lazy val root = project.in( file(".") )
  .settings(
    name := infoPub.projectName,
    organization := infoPub.orgNameFull,
    organizationHomepage := Some(new URL("http://github.com/sequenceplanner")),
    description := "A sbt library for SP-Domain",
    scalaVersion := versions.scala,
    version := infoPub.spDomainVersion,
    // use gpg - command line
    useGpg := true,
    // PUBLISHING
    pomIncludeRepository := { _ => false },
    publishArtifact in Test := false
  )

// publishing info
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

// SP-Domain
lazy val spdomain = (crossProject.crossType(CrossType.Pure) in file("spdomain"))
  .settings(
    name := SPSettings.projectName,
    version := PublishingSettings.infoPub.spDomainVersion
  )
  .settings(commonSettings)
  .settings(libraryDependencies ++= domainDependencies.value)
  .jvmSettings(
    libraryDependencies += "org.joda" % "joda-convert" % "1.8.2"
  )
  .jsSettings(
    jsSettings
  )

lazy val spdomain_jvm = spdomain.jvm
lazy val spdomain_js = spdomain.js

