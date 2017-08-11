name := "SequencePlanner"
version := "2.1"
scalaOrganization := "org.scala-lang"
scalaVersion := "2.12.3"

lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.3",
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.5.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3",
  "org.slf4j" % "slf4j-simple" % "1.7.7",
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val json = Seq(
  "com.typesafe.play" %% "play-json" % "2.6.0",
  "org.julienrf" %% "play-json-derived-codecs" % "4.0.0",
  "io.github.cquiroz" %% "scala-java-time" % "2.0.0-M12",
  "org.joda" % "joda-convert" % "1.8.2"
)

lazy val support = Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

lazy val commonSettings = Seq(
//  scalaOrganization := "org.typelevel",
//  scalaVersion := "2.12.3-bin-typelevel-4",
   scalaOrganization := "org.scala-lang",
   scalaVersion := "2.12.3",
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/Releases",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"),
  scalacOptions  := Seq(
    "-encoding", "utf8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-language:implicitConversions",
    "-language:postfixOps"
  ),
  fork := true,
  javaOptions += s"-Dconfig.file=${root.base.getCanonicalPath}/cluster.conf",
  connectInput in run := true,
  version := "2.0_M1"
)


lazy val root = project.in( file(".") )
  //.aggregate(spdomain, spcore, macros, gui)

lazy val spdomain = project
  .settings(commonSettings: _*)

lazy val spcore = project
  .dependsOn(spdomain, macros)
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= akka)
  .settings(libraryDependencies ++= json)



lazy val macros = project
  .settings(commonSettings: _*)
  .settings(libraryDependencies += scalaReflect.value)
  .settings(libraryDependencies ++= json)

lazy val gui = project

lazy val sp1 = project
  .settings(commonSettings: _*)


lazy val labkit = (project in file("spservices/labkit"))
  .dependsOn(spdomain, macros, devicehandler, operationRunners)
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= akka)

lazy val devicehandler = (project in file("spservices/devicehandler"))
  .dependsOn(spdomain)
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= akka)

lazy val modelsTest = (project in file("spservices/modelsTest"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val opcua = (project in file("spservices/opcua"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val fakeElvisService = (project in file("spservices/fakeElvisService"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val widgetService = (project in file("spservices/widgetService"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val ericaBackend = (project in file("spservices/gPubSubService"))
  .dependsOn(spdomain, macros, spcore, widgetService)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val exampleService = (project in file("spservices/exampleService"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val operationRunners = (project in file("spservices/operationRunners"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val d3exampleService = (project in file("spservices/d3exampleservice"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val itemServiceDummy = (project in file("spservices/itemServiceDummy"))
  .dependsOn(spdomain, macros)
.settings(commonSettings: _*)
.settings(libraryDependencies ++= akka)

lazy val spseed = (project in file("spservices/spseed"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= akka)


// lazy val models = project.dependsOn(domain, extensions).
//   settings(commonSettings: _*).
//   settings(libraryDependencies ++= akka ++ json ++ support)

// lazy val macros = project.
//   settings(commonSettings: _*).
//   settings(libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)).
//   settings(libraryDependencies ++= json)


// lazy val core = project.dependsOn(domain).
//   settings(commonSettings: _*).
//   settings(libraryDependencies ++= akka ++ json)


// lazy val gui = project.dependsOn(domain, core).
//   settings(commonSettings: _*).
//   settings(libraryDependencies ++= akka ++ json)

// lazy val extensions = project.dependsOn(domain, core).
//   settings(commonSettings: _*).
//   settings(libraryDependencies ++= akka ++ json)


// lazy val launch = project.dependsOn(domain, core, gui, extensions).
//   settings(commonSettings: _*).
//   settings(libraryDependencies ++= akka ++ json)

// lazy val services = project.dependsOn(domain, extensions).
//   settings(commonSettings: _*).
//   settings(libraryDependencies ++= akka ++ json ++ support)
