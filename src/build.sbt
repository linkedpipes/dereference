name := "discovery"

version := "1.0"

lazy val `discovery` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers ++= Seq(
    "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    specs2 % Test,
    "org.apache.jena" % "apache-jena" % "3.2.0",
    "org.scalaj" %% "scalaj-http" % "2.3.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

fork in run := false

routesGenerator := InjectedRoutesGenerator