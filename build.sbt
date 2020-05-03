name := "project"

version := "0.1"

scalaVersion := "2.13.2"

// https://mvnrepository.com/artifact/com.github.ghostdogpr/caliban-client
libraryDependencies += "com.github.ghostdogpr" %% "caliban-client" % "0.7.6"

// https://mvnrepository.com/artifact/com.softwaremill.sttp.client/async-http-client-backend-zio
libraryDependencies += "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.1.0"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"


enablePlugins(CodegenPlugin)