import com.typesafe.sbt.SbtScalariform.*

import scalariform.formatter.preferences.*

name := "prayerping-web"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.13.12"

resolvers += Resolver.jcenterRepo

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

val versions = new Object {
  val silhouette = "10.0.0"
  val playMailer = "10.0.0"
}
val deps = new Object {
  def silhouette(post: String) = "org.playframework.silhouette" %% s"play-silhouette$post" % versions.silhouette
}

libraryDependencies ++= Seq(
  deps.silhouette(""),
  deps.silhouette("-password-bcrypt"),
  deps.silhouette("-persistence"),
  deps.silhouette("-crypto-jca"),
  deps.silhouette("-totp"),
  "org.bouncycastle" % "bcprov-jdk18on" % "1.78.1",
  "com.fasterxml.uuid" % "java-uuid-generator" % "4.3.0",
  "net.codingwell" %% "scala-guice" % "5.1.1",
  "com.iheart" %% "ficus" % "1.5.2",
  "org.playframework" %% "play-mailer" % versions.playMailer,
  "org.playframework" %% "play-mailer-guice" % versions.playMailer,
  "io.github.samueleresca" %% "pekko-quartz-scheduler" % "1.2.0-pekko-1.0.x",
  "com.github.tminglei" %% "slick-pg" % "0.22.2",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.22.2",
  "org.playframework" %% "play-slick" % "6.1.1",
  "org.playframework" %% "play-slick-evolutions" % "6.1.0",
  "org.postgresql" % "postgresql" % "42.7.3",
  "io.github.rediscala" %% "rediscala" % "1.14.1-pekko",
  deps.silhouette("-testkit") % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % "test",
  ehcache,
  guice,
  filters
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesImport += "org.prayerping.utils.route.Binders._"

// https://github.com/playframework/twirl/issues/105
TwirlKeys.templateImports := Seq()

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  //"-Xlint", // Enable recommended additional warnings.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  // Play has a lot of issues with unused imports and unsued params
  // https://github.com/playframework/playframework/issues/6690
  // https://github.com/playframework/twirl/issues/105
  "-Xlint:-unused,_"
)

//********************************************************
// Scalariform settings
//********************************************************

scalariformAutoformat := true

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentConstructorArguments, false)
  .setPreference(DanglingCloseParenthesis, Preserve)

//********************************************************
// Dev settings for WebSocket keepalive
//********************************************************
PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-max-idle" -> "10 seconds"
PlayKeys.devSettings += "play.server.websocket.periodic-keep-alive-mode" -> "pong"
