import SPSettings._

//import sbt.Keys.{pomExtra, pomIncludeRepository}


lazy val buildSettings = Seq(
  name         := "sp-domain",
  description  := "The domain and logic to work with it",
  version      := "0.0.1-SNAPSHOT",
  organization := "com.github.sequenceplanner",
  homepage     := Some(new URL("http://github.com/sequenceplanner")),
  licenses     := Seq("MIT License" -> url("https://opensource.org/licenses/MIT")),

  scalaVersion       := versions.scala,
  
  scalacOptions ++= scalacOpt,

  useGpg := true,

  publishArtifact in Test := false,
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
    libraryDependencies ++= domainDependencies.value,
    pomIncludeRepository := { _ => false },
    sonatypeProfileName := "com.github.sequenceplanner",
scmInfo := Some(
      ScmInfo(
        url("https://github.com/sequenceplanner/sp-domain"),
        "scm:git@github.com:sequenceplanner/sp-domain.git"
      )
    ),
developers := List(
  Developer(
      id    = "aleastChs",
      name  = "Alexander Åstrand",
      email = "aleast@student.chalmers.se",
      url   = url("https://github.com/aleastChs")
    )
  )
)


lazy val root = project.in(file("."))
  .aggregate(spdomain_jvm, spdomain_js)
  .settings(commonSettings)
  .settings(buildSettings)
  .settings(
    name                 := "sp-domain",
    // No, SBT, we don't want any artifacts for root.
    // No, not even an empty jar.
    publish              := {},
    publishLocal         := {},
    publishArtifact      := false,
    Keys.`package`       := file("")
    )


lazy val spdomain = (crossProject.crossType(CrossType.Pure) in file("."))
  .settings(commonSettings)
  .settings(buildSettings)
  .jvmSettings(
    libraryDependencies += "org.joda" % "joda-convert" % "1.8.2"
  )
  .jsSettings(
    jsSettings
  )

lazy val spdomain_jvm = spdomain.jvm
lazy val spdomain_js = spdomain.js
