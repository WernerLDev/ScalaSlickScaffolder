package werlang.spec

import scala.io.Source
import play.api.libs.json._
import play.api.libs.json.Reads._

case class SpecReader(file:String) {

    implicit val SpecAttrReads = Json.reads[EntityAttribute]
    implicit val SpecRelation = Json.reads[EntityRelation]
    implicit val SpecEntityReads = Json.reads[SpecEntity]
    implicit val SpecReads = Json.reads[SpecFile]

    def getData = {
        val inputjson = Source.fromFile(file).getLines.mkString
        Json.parse(inputjson).validate[SpecFile]
    }
}