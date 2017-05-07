name := "hyrule"

version := "1.0"

scalaVersion := "2.12.1"

resolvers += "Spigot Repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"

resolvers += "BungeeCord Repo" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += Resolver.mavenLocal

libraryDependencies += "org.spigotmc" % "spigot-api" % "1.11.2-R0.1-SNAPSHOT" % "provided"

libraryDependencies += "org.bukkit" % "bukkit" % "1.11.2-R0.1-SNAPSHOT" % "provided"

libraryDependencies += "org.spigotmc" % "minecraft-server" % "1.11.2-SNAPSHOT" % "provided"

libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value % "provided"

scalacOptions += "-deprecation"

