package werlang

import werlang.scaffolder._
import play.api.libs.json._
import java.io._

object Main extends App {

    def handleErrors(e:JsError) {
        println(scala.Console.RED + "*******************************")
        println("* "+scala.Console.RESET+"Error parsing Json :( " + scala.Console.RED + "      *")
        println(scala.Console.RED + "*******************************" + scala.Console.RESET)
        println(" ")
        e.errors.foreach {
            case (path, validationErrors) => {
                println(scala.Console.RED + "###############################" + scala.Console.RESET)
                println("> Path: '" + path + "': ")
                validationErrors.foreach(error => {
                    println("> Error: " + error.message)
                })
                println(scala.Console.RED + "###############################" + scala.Console.RESET)

                println(" ")
            }
        }
    }

    def addIds(obj:SpecEntity) = {
        val idAttr = EntityAttribute("id", "key")
        val nameAttr = EntityAttribute("name", "string")
        val newAttributes = idAttr :: nameAttr :: obj.attributes
        val relations = obj.relations.filter(x => x.has == "one").map(relation => {
            EntityAttribute(relation.of.toLowerCase + "_id", "Long")
        })
        obj.copy(attributes = newAttributes ++ relations)
    }

    def forceLowerCase(obj:SpecEntity) = {
        obj.copy(name = obj.name.toLowerCase, plural = obj.plural.toLowerCase)
    }

    override def main(args:Array[String]) {
      val specfile = {
          if(args.length > 0) args(0)
          else "SlickScaffolder/specs/test.json"
      } 
      val jsonfile = new File(specfile)
      if(jsonfile.exists == false) {
            println(scala.Console.RED + "**************************************")
            println("* Error: " + scala.Console.RESET+"Could not find JSON file :( " + scala.Console.RED + "*")
            println(scala.Console.RED + "**************************************" + scala.Console.RESET)
          
          return;
      }
      val reader = ReadSpec(specfile)
      reader.getData match {
          case x:JsSuccess[SpecFile] => {
            val imports = ImportGenerator(x.get).generate
            val entities = x.get.entities.map(entity => forceLowerCase(addIds(entity)))
            println(scala.Console.CYAN +    "*****************************************************")
            println("*"+scala.Console.RESET+"          Loaded JSON file. Starting magic!        "+scala.Console.CYAN+"*")
            println("*****************************************************" + scala.Console.RESET)
            val pw = new PrintWriter(new File(x.get.modelFolder + "/Tables.scala"))
            pw.write(imports)
            entities.foreach(entity => {
                val generator = Generator(entity, entities)
                //val pw = new PrintWriter(new File(x.get.modelFolder + "/" + entity.name + ".scala" ))
                //pw.write(imports)
                pw.write(generator.generate)
                val modelFile = new File(x.get.modelFolder + "/" + entity.name + ".scala")
                if(modelFile.exists) {
                    println(scala.Console.GREEN + "[OK] "+scala.Console.RESET+"Skipping  " + x.get.modelFolder + "/" +  entity.name + ".scala, Already exists.")
                } else {
                    val classpw = new PrintWriter(modelFile)
                    classpw.write(imports)
                    classpw.write(generator.generateClass)
                    classpw.close()
                    println(scala.Console.GREEN + "[OK] "+scala.Console.RESET+"Generated " + x.get.modelFolder + "/" +  entity.name + ".scala")
                }
            })
            pw.close()
            println(scala.Console.GREEN + "[OK] "+scala.Console.RESET+"Generated " + x.get.modelFolder + "/Tables.scala")
            val migrations = MigrationGenerator(entities)
            val pw2 = new PrintWriter(new File(x.get.migrationFile))
            pw2.write(migrations.generate)
            pw2.close();
            println(scala.Console.GREEN + "[OK] "+scala.Console.RESET + "Generated " +  x.get.migrationFile)

            println(scala.Console.GREEN +    "*****************************************************")
            println("*"+scala.Console.RESET+"                    All Done !                     "+scala.Console.GREEN+"*")
            println("*****************************************************" + scala.Console.RESET)
          }
          case e: JsError => handleErrors(e)
      }
  }
}