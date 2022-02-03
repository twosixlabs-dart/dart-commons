import Dependencies._
import sbt._

organization in ThisBuild := "com.twosixlabs.dart"
name := "dart-commons"
scalaVersion in ThisBuild := "2.12.7"
crossScalaVersions in ThisBuild := Seq( "2.11.12", "2.12.7" )

resolvers in ThisBuild ++= Seq( "Maven Central" at "https://repo1.maven.org/maven2/",
                                "JCenter" at "https://jcenter.bintray.com",
                                "Local Ivy Repository" at s"file://${System.getProperty( "user.home" )}/.ivy2/local/default" )

crossScalaVersions in ThisBuild := Seq( "2.11.12", "2.12.7" )

test in publish in ThisBuild := {}
test in publishLocal in ThisBuild := {}
publishMavenStyle := true

lazy val root = ( project in file( "." ) ).aggregate( awsCommon, json, cli, testBase, utils, exceptions, sql )

lazy val json = ( project in file( "dart-json" ) ).settings( libraryDependencies ++= jackson ).dependsOn( testBase % Test )

lazy val cli = ( project in file( "dart-cli" ) ).settings( libraryDependencies ++= scopt
                                                                                   ++ betterFiles
                                                                                   ++ logging
                                                                                   ++ scalaTest ).dependsOn( testBase % Test )

lazy val utils = ( project in file( "dart-utils" ) ).settings( libraryDependencies ++= jackson
                                                                                       ++ jsonValidator
                                                                                       ++ scalaTest
                                                                                       ++ scalaMock
                                                                                       ++ logging ).dependsOn( testBase % Test )

lazy val awsCommon = ( project in file( "dart-aws" ) ).settings( libraryDependencies ++= aws
                                                                                         ++ betterFiles
                                                                                         ++ scala12Extensions
                                                                                         ++ s3mock
                                                                                         ++ scalaTest
                                                                                         ++ logging,
                                                                 dependencyOverrides ++= Seq( "org.codehaus.stax2" % "stax2-api" % "3.1.4",
                                                                                              "com.fasterxml.woodstox" % "woodstox-core" % "5.0.2" ) ).dependsOn( testBase % Test )

lazy val exceptions = ( project in file( "dart-exceptions" ) ).dependsOn( testBase % Test ).settings( libraryDependencies ++= logging )

lazy val testBase = ( project in file( "dart-test-base" ) ).settings( libraryDependencies ++= logging ++ scalaTestCompileScope ++ mockito )

//@formatter:on

lazy val sql = ( project in file( "dart-sql" ) ).settings( libraryDependencies ++= database ++ logging ).dependsOn( testBase % Test )

javacOptions in ThisBuild ++= Seq( "-source", "8", "-target", "8" )
scalacOptions in ThisBuild += "-target:jvm-1.8"
