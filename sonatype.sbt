import PublishingSettings._

// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "sequenceplanner2"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// License of your choice
licenses := mitLicense
homepage := githubSP
scmInfo := scmSPDomain
developers := List(
  devAlexander
)
