import SPSettings._

lazy val root = project.in( file(".") )
//.aggregate(SPSettings.spdomain, SPSettings.spcomm, SPSettings.spcore, SPSettings.spgui)


lazy val spdomain = (crossProject.crossType(CrossType.Pure) in file("spdomain"))
  .settings(name := "spdomain")
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

// PUBLISHING

// use gpg - command line
useGpg := true

// let’s make sure no repositories show up in the POM file and
//  remove the repositories for optional dependencies in our artifact
pomIncludeRepository := { _ => false }

licenses := mitLicense

homepage := githubSP

scmInfo := scmSPDomain

developers := List(
  // student
  devAlexander
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")