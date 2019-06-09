name := """IoCom"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.7")

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.0.0"
libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "net.sf.ucanaccess" % "ucanaccess" % "4.0.4"
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"                  % "3.3.2",
  "org.scalikejdbc" %% "scalikejdbc-config"           % "3.3.2",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.7.0-scalikejdbc-3.3"
)

