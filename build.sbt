
import sbt.Keys._

import sbtdocker.BuildOptions.Remove.Always
import sbtrelease.ReleaseStateTransformations._

name := "safer-gambling-app"

version := "0.1"

scalaVersion := "2.12.11"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.5" % "test", "org.scalactic" %% "scalactic" % "3.0.5")

resolvers ++= Seq(

  "Confluent" at "https://packages.confluent.io/maven/",
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "Artima Maven Repository" at "https://repo.artima.com/releases",
  "Artifactory" at "https://artifactory.euw.platformservices.io/artifactory/sbg-next-gen-promotions-sbt/",
  "GPROM_Artifactory" at "https://artifactory.euw.platformservices.io/artifactory/sbg-gaming-promotions-sbt-local/",
  "jitpack" at "https://jitpack.io/",
  "gseitz@github" at "https://gseitz.github.com/maven/"
)

enablePlugins(DockerPlugin)

mainClass in Compile := Some("myApp.Application")





credentials += Credentials("Artifactory Realm", "artifactory.euw.platformservices.io", sys.env.getOrElse("ARTIFACTORY_USERNAME", "n/a"), sys.env.getOrElse("ARTIFACTORY_APIKEY", "n/a"))



assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assemblyMergeStrategy in assembly := {
  case PathList("org", "slf4j", xs@_*) => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


docker := docker.dependsOn(assembly).value

dockerfile in docker := {
  val artifact = (assemblyOutputPath in assembly).value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("docker.artifactory.euw.platformservices.io/sbg-next-gen-promotions/java-sbg:8")
    add(assembly.value, artifactTargetPath)
    runRaw(s"java -cp $artifactTargetPath com.skybet.gaming.ngp.cli.GenerateAutoCompletionFunction /root/.bashrc")
    runRaw(s"""echo 'alias ngp-cli="java -jar $artifactTargetPath"' >> /root/.bashrc""")
    cmd("java", "-jar", artifactTargetPath)
  }
}
buildOptions in docker := BuildOptions(removeIntermediateContainers = Always, pullBaseImage = sbtdocker.BuildOptions.Pull.Always)

val dockerRegistry = "docker.artifactory.euw.platformservices.io/"

imageNames in docker := Seq(
  ImageName(s"${dockerRegistry}sbg-next-gen-promotions/${name.value}:${sys.props.getOrElse("VERSION_NUMBER", version.value)}")
)

def publishSnapshot = Command.command("publish-snapshot") { state =>
  val ourVersion = sys.props.get("VERSION_NUMBER").get
  val newState = Command.process(s"""set version := "$ourVersion-SNAPSHOT" """, state)
  val (s, _) = Project.extract(newState).runTask(publish in Compile, newState)
  s
}

commands += publishSnapshot

publishTo := version { v: String =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some("Artifactory" at "https://artifactory.euw.platformservices.io/artifactory/sbg-next-gen-promotions-sbt;build.timestamp=" + System.currentTimeMillis())
  else
    Some("Artifactory" at "https://artifactory.euw.platformservices.io/artifactory/sbg-next-gen-promotions-sbt")
}.value