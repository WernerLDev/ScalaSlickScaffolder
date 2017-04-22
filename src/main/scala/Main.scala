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
        val newAttributes = idAttr :: obj.attributes
        val relations = obj.relations.filter(x => x.has == "one").map(relation => {
            EntityAttribute(relation.of.toLowerCase + "_id", "Long")
        })
        obj.copy(attributes = newAttributes ++ relations)
    }

    def forceLowerCase(obj:SpecEntity) = {
        obj.copy(name = obj.name.toLowerCase, plural = obj.plural.toLowerCase)
    }

    override def main(args:Array[String]) {
      val reader = ReadSpec("specs/test.json")
      reader.getData match {
          case x:JsSuccess[SpecFile] => {
            val imports = ImportGenerator(x.get).generate
            val entities = x.get.entities.map(entity => forceLowerCase(addIds(entity)))
            println(scala.Console.BLUE + "*****************************************************")
            println("*"+scala.Console.RESET+" Loaded JSON file without errors. Starting magic ! "+scala.Console.BLUE+"*")
            println("*****************************************************" + scala.Console.RESET)
            entities.foreach(entity => {
                val generator = Generator(entity, entities)
                val pw = new PrintWriter(new File("output/" + entity.name + ".scala" ))
                pw.write(imports)
                pw.write(generator.generate)
                pw.close()
                println(scala.Console.GREEN + "[OK] "+scala.Console.RESET+"Generated " + entity.name)
            })
            println(scala.Console.GREEN +    "*****************************************************")
            println("*"+scala.Console.RESET+"                    All Done !                     "+scala.Console.GREEN+"*")
            println("*****************************************************" + scala.Console.RESET)
          }
          case e: JsError => handleErrors(e)
      }
  }
}
