import com.typesafe.sbt.SbtPgp.autoImportImpl.useGpg
import sbt.{Def, Developer, ScmInfo, url}
import sbt.Keys.{licenses, pomIncludeRepository, publishMavenStyle, scmInfo, _}

object PublishingSettings {
  lazy val projectName = "sequenceplanner"
  lazy val orgName = "com.github"+ projectName

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

  lazy val publishing = Seq(
    organization := orgName,
    // use gpg - command line
    useGpg := true,
    // let’s make sure no repositories show up in the POM file and
    //  remove the repositories for optional dependencies in our artifact
    pomIncludeRepository := { _ => false }
  )

}