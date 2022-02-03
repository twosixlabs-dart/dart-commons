import sbt._

object Dependencies {

    val slf4jVersion = "1.7.20"
    val logbackVersion = "1.2.9"
    val betterFilesVersion = "3.8.0"
    val scoptVersion = "4.0.0-RC2"

    val kafkaVersion = "2.2.1"

    val jacksonVersion = "2.9.9"
    val scala12ExtensionsVersion = "0.9.0"

    val scalaMockVersion = "4.2.0"
    val embeddedKafkaVersion = "2.2.0"
    val scalaTestVersion = "3.1.4"

    val awsVersion = "2.14.7"
    val s3mockVersion = "2.1.19"

    val scalatraVersion = "2.7.0"
    val servletApiVersion = "3.1.0"

    val postgresVersion = "42.2.10"
    val h2Version = "1.4.200"
    val c3p0Version = "0.9.5.1"

    val mockitoVersion = "1.16.0"


    val logging = Seq( "org.slf4j" % "slf4j-api" % slf4jVersion,
                       "ch.qos.logback" % "logback-classic" % logbackVersion )

    val betterFiles = Seq( "com.github.pathikrit" %% "better-files" % betterFilesVersion )

    /// testing
    val scalaMock = Seq( "org.scalamock" %% "scalamock" % scalaMockVersion % Test )

    val scalaTest = Seq( "org.scalatest" %% "scalatest" % scalaTestVersion % Test )

    val scalaTestCompileScope = Seq( "org.scalatest" %% "scalatest" % scalaTestVersion )


    val scopt = Seq( "com.github.scopt" %% "scopt" % scoptVersion )

    // streaming
    val kafka = Seq( "org.apache.kafka" %% "kafka" % kafkaVersion,
                     "org.apache.kafka" % "kafka-clients" % kafkaVersion,
                     "org.apache.kafka" % "kafka-streams" % kafkaVersion,
                     "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion )

    val embeddedKafka = Seq( "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion % Test,
                             "io.github.embeddedkafka" %% "embedded-kafka-streams" % embeddedKafkaVersion % Test,
                             "jakarta.ws.rs" % "jakarta.ws.rs-api" % "2.1.2" % Test ) //https://github.com/sbt/sbt/issues/3618


    val scala12Extensions = Seq( "org.scala-lang.modules" %% "scala-java8-compat" % scala12ExtensionsVersion )

    // JSON
    val jackson = Seq( "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
                       "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion )

    val jsonValidator = Seq( "org.everit.json" % "org.everit.json.schema" % "1.5.1" )

    // AWS
    val aws = Seq( "software.amazon.awssdk" % "ec2" % awsVersion,
                   "software.amazon.awssdk" % "s3" % awsVersion,
                   "software.amazon.awssdk" % "sts" % awsVersion )

    val s3mock = Seq( "com.adobe.testing" % "s3mock" % s3mockVersion % Test )

    val scalatra = Seq( "org.scalatra" %% "scalatra" % scalatraVersion,
                        "javax.servlet" % "javax.servlet-api" % servletApiVersion,
                        "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % Test )

    val database = Seq( "org.postgresql" % "postgresql" % postgresVersion,
                        "com.h2database" % "h2" % h2Version,
                        "com.mchange" % "c3p0" % c3p0Version )


    val mockito = Seq( "org.mockito" %% "mockito-scala-scalatest" % mockitoVersion )

}
