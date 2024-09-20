// Comment to get more information during initialization
logLevel := Level.Warn

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
