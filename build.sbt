name := "blog-api"
version := "1.0"
scalaVersion := "2.13.10"

val AkkaVersion = "2.6.20"
val AkkaHttpVersion = "10.2.10"
val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  
  // JSON (Circe)
  "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  
  // Database
  "org.postgresql" % "postgresql" % "42.5.4",
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  "org.flywaydb" % "flyway-core" % "9.16.0",
  
  // JWT
  "com.pauldijou" %% "jwt-core" % "5.0.0",
  
  // Config
  "com.typesafe" % "config" % "1.4.2",
  
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  
  // Security
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test
)