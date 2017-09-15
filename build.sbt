import SPSettings._
import PublishingSettings._

lazy val root = project.in( file(".") )
  .settings(publishing)
  .settings(
    name := "sp-domain",
    scalaVersion := "2.12.2"
  )

// PUBLISHING
pomIncludeRepository := { _ => false }
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
    </developers>
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
//publishTo := {
//  val nexus = "https://oss.sonatype.org/"
//  if (isSnapshot.value)
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}
publishArtifact in Test := false

// SP-Domain
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

