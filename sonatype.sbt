import PublishingSettings._

// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := infoPub.orgNameFull

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// License MIT
licenses := infoPub.mitLicense
homepage := infoPub.githubSP
scmInfo := infoPub.scmSPDomain
developers := List(
  infoPub.devAlexander
)
description := infoPub.description
