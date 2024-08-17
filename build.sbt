import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

lazy val scalaFXVersion = "22.0.0-R33"
lazy val javaFXVersion = "22"

lazy val scalaFXDerivation = (project in file("."))
  .settings(
    name := "ScalaFXDerivation",
    idePackagePrefix := Some("com.joshuapetersen.scala.scalafx.derivation"),
    libraryDependencies += "org.scalafx" %% "scalafx" % scalaFXVersion withJavadoc() withSources(),
    libraryDependencies ++= {
      // Determine OS version of JavaFX binaries
      lazy val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux") => "linux"
        case n if n.startsWith("Mac") => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % javaFXVersion classifier osName)
        .map(_ withJavadoc() withSources())
    }
  )
