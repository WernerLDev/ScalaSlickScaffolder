package scaffolder

import scala.io.Source
import play.api.libs.json._
import play.api.libs.json.Reads._


case class EntityAttribute(name:String, atype:String)
case class SpecEntity(name:String, plural:String, attributes:List[EntityAttribute])
case class SpecFile(packageName:String, entities:List[SpecEntity])

case class ReadSpec(file:String) {

    implicit val SpecAttrReads = Json.reads[EntityAttribute]
    implicit val SpecEntityReads = Json.reads[SpecEntity]
    implicit val SpecReads = Json.reads[SpecFile]

    def getData = {
        val inputjson = Source.fromFile(file).getLines.mkString
        Json.parse(inputjson).validate[SpecFile]
    }
}