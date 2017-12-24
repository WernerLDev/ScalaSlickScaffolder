package werlang

import play.api.libs.json._
import java.io._

import werlang.generator.CodeGenerator
import werlang.spec._

object Main {


  def main(args:Array[String]) {
    val specFile = {
      if(args.length > 0) args(0)
      else "specs/test.json"
    } 
    val jsonFile = new File(specFile)
    if(!jsonFile.exists) {
      Message.error("Could not find JSON file :(")
      return;
    }

    val reader = SpecReader(specFile)
    reader.getData match {
      case e:JsError => Message.handleParseErrors(e)
      case x:JsSuccess[SpecFile] => {
        Message.info("JSON file loaded, checking spec...")
        val specFile = x.get

        val entities = specFile.entities.map(entity => 
          Preprocessor.forceLowerCase(Preprocessor.addIds(entity))
        )
        
        if(!CheckSpec.check(entities)) {
          Message.error("Check failed")
        } else {
          val processedSpec = specFile.copy(entities = entities)
          val relationEntities = entities.flatMap(x => 
            Preprocessor.createRelationEntities(x, entities)
          )
          CodeGenerator(processedSpec, relationEntities).generate()

          Message.green("All finished !")
        }

      }
    }
  }
}
