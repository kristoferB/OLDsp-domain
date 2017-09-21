
import sbt.{ Developer, ScmInfo, url}

object PublishingSettings {

  object infoPub {
    // Description
    lazy val description = "THIS IS OUR DOMAIN FOR SP"

    // Name
    lazy val organizationName = "sequenceplanner"
    lazy val comString = "com"
    lazy val githubString = "github"

    lazy val orgNameFull = comString +"."+ githubString +"."+ organizationName
    lazy val groupIdSonatype = comString +"."+ githubString +"."+ organizationName

    lazy val projectName = "sp-domain"

    // Version
    lazy val spDomainVersion = "0.0.1-SNAPSHOT"

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
}