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
    // EXTRA POM
    pomExtra :=
      <licenses>
        <license>
          <name>MIT License</name>
          <url>https://opensource.org/licenses/MIT</url>
        </license>
      </licenses>
        <developers>
          <developer>
            <id>aleastChs</id>
            <name>Alexander Åstrand (@chalmersUniversity)</name>
            <url>https://github.com/aleastChs/</url>
          </developer>
        </developers>,

        publishArtifact in Test := false
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

