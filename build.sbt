name := "project"

version := "0.1"

scalaVersion := "2.13.2"

// https://mvnrepository.com/artifact/com.github.ghostdogpr/caliban-client
libraryDependencies += "com.github.ghostdogpr" %% "caliban-client" % "0.7.6"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"

// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.4.0"

// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.24.0"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

enablePlugins(CodegenPlugin)