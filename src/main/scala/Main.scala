import scaffolder._
import play.api.libs.json._
import java.io._

object Main extends App {

    def handleErrors(e:JsError) {
        println("Error parsing Json: ")
        println(" ")
        e.errors.foreach {
            case (path, validationErrors) => {
                println("> Path: '" + path + "': ")
                validationErrors.foreach(error => {
                    println("> Error: " + error.message)
                })
                println(" ")
            }
        }
    }

    def addIds(obj:SpecEntity) = {
        val idAttr = EntityAttribute("id", "key")
        obj.copy(attributes = idAttr :: obj.attributes)
    }

    def forceLowerCase(obj:SpecEntity) = {
        obj.copy(name = obj.name.toLowerCase, plural = obj.plural.toLowerCase)
    }

    override def main(args:Array[String]) {
      val reader = ReadSpec("specs/test.json")
      reader.getData match {
          case x:JsSuccess[SpecFile] => {
            val imports = ImportGenerator(x.get).generate
            println("Loaded JSOn file without errors. Starting magic !")
            x.get.entities.foreach(entity => {
                val e = forceLowerCase(addIds(entity))
                val generator = Generator(e)
                val pw = new PrintWriter(new File("output/" + e.name + ".scala" ))
                pw.write(imports)
                pw.write(generator.generate)
                pw.close()
                println("Generated " + e.name)
            })
            println("All done.")
          }
          case e: JsError => handleErrors(e)
      }
  }
}
