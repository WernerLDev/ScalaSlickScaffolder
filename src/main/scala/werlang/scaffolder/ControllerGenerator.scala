package werlang.scaffolder
import werlang.scaffolder._

case class ControllerGenerator(all:List[SpecEntity]) {

    val traittpl:String = """|
                        |trait TGenController {
                        |    def getAll(request:AuthRequest[AnyContent]):Future[Result]
                        |    def insert(request:AuthRequest[JsValue]):Future[Result]
                        |    def delete(id:Long, request:AuthRequest[AnyContent]):Future[Result]
                        |    def createNew(request:AuthRequest[AnyContent]):Future[Result]
                        |}
                        |""".stripMargin

    val tpl:String = """
                    |@Singleton
                    |class Generated{pluralWC}Controller @Inject() (
                    |    WithAuthAction:AuthAction,
                    |    {plural}:{pluralWC}
                    |    ) extends Controller with TGenController {
                    |
                    |    implicit val tsreads: Reads[Timestamp] = Reads.of[Long] map (new Timestamp(_))
                    |    implicit val {nameWC}Writes = Json.writes[{nameWC}]
                    |    implicit val {nameWC}Reads = Json.reads[{nameWC}]
                    |{relationReads}
                    |
                    |    def getAll(request:AuthRequest[AnyContent]) = {
                    |        {getstr}
                    |    }
                    |
                    |    def insert(request:AuthRequest[JsValue]) = {
                    |        {request.body \ "entity"}.asOpt[{nameWC}].map( entity => {
                    |            {plural}.insert(entity).map(x => {
                    |                Ok(Json.toJson( Map("id" -> JsNumber(x.id)) ))
                    |            })
                    |        }).getOrElse(Future(BadRequest("Parameter missing")))
                    |    }
                    |
                    |    def delete(id:Long, request:AuthRequest[AnyContent]) = {
                    |      {plural}.delete(id).map(x => Ok(Json.toJson(Map("success" -> JsBoolean(true)))))
                    |    }
                    |
                    |    def createNew(request:AuthRequest[AnyContent]) = {
                    |        {plural}.insert({nameWC}(
                    |           {initialValues} 
                    |        )) map (x => Ok(Json.toJson(x)))
                    |    }
                    |
                    |}
                    |""".stripMargin


    val tplController = """|
                        |@Singleton
                        |class GeneratedController @Inject() (
                        |    WithAuthAction:AuthAction,
                        |{controllers}
                        |) extends Controller {
                        |
                        |
                        |    val controllers = Map(
                        |{controllermap}
                        |    )
                        | 
                        |    val entityTypes = Map(
                        |{entityTypes}
                        |    )
                        |
                        |
                        |    def getAll(name:String) = WithAuthAction.async { request =>
                        |        controllers.get(name) match {
                        |            case Some(x) => x.getAll(request)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }
                        |    }
                        |
                        |    def insert(name:String) = WithAuthAction.async(parse.json) { request =>
                        |        controllers.get(name) match {
                        |            case Some(x) => x.insert(request)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }
                        |    }
                        |
                        |    def delete(name:String, id:Long) = WithAuthAction.async { request =>
                        |        controllers.get(name) match {
                        |            case Some(x) => x.delete(id, request)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }
                        |    }
                        |
                        |    def createNew(name:String) = WithAuthAction.async { request =>
                        |        controllers.get(name) match {
                        |            case Some(x) => x.createNew(request)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }
                        |    }
                        |
                        |    def getEntities = WithAuthAction { request =>
                        |        val entities = entityTypes.map { case (k,v) => {
                        |            Json.toJson(Map("name" -> JsString(k.capitalize), "plural" -> JsString(v)))
                        |        }}.toSeq
                        |        Ok( Json.toJson(JsArray(entities)) )
                        |    }
                        |}
                        |""".stripMargin

    def generate = {
        traittpl + all.map(entity => {
            val getstr = {
                if(entity.relations.length == 0) {
                    entity.plural + ".getAll.map(x => Ok(Json.toJson(x)))"
                } else {
                    entity.plural + ".getAll.map(entities => Ok(Json.toJson(entities.map(x => "+ generateMap(entity) +" ))))"
                }
            }
            tpl.replaceAll("\\{name\\}", entity.name.toLowerCase)
               .replaceAll("\\{nameWC\\}", entity.name.toLowerCase.capitalize)
               .replaceAll("\\{plural\\}", entity.plural.toLowerCase)
               .replaceAll("\\{pluralWC\\}", entity.plural.toLowerCase.capitalize)
               .replaceAll("\\{getstr\\}", getstr)
               .replaceAll("\\{relationReads\\}", generateReads(entity))
               .replaceAll("\\{initialValues\\}", generateInitialValues(entity.attributes))
        }).mkString("\n") + generateMainController
    }

    def generateReads(entity:SpecEntity) = {
        entity.relations.map(x => {
            "    implicit val " + x.of + "Writes = Json.writes["+x.of.capitalize+"]"
        }).mkString("\n")
    }

    def generateInitialValues(attributes:List[EntityAttribute]) = {
        attributes.map(a => {
            val longs = List("long", "key")
            var strings = List("text", "string")
            if(longs.contains(a.atype.toLowerCase)) "0"
            else if(a.atype.toLowerCase == "timestamp") "new Timestamp(new java.util.Date().getTime())"
            else "\"\""
        }).mkString(", ")
    }

    def getMapField(x:EntityAttribute) = {
        val longs = List("long", "key")
        var strings = List("text", "string")
        if(longs.contains(x.atype.toLowerCase)) {
            "\"" + x.name + "\" -> JsNumber(x._1." + x.name + ")"
        } else if(x.atype.toLowerCase == "timestamp") {
            "\"" + x.name + "\" -> JsNumber(x._1." + x.name + ".getTime())"
        } else {
            "\"" + x.name + "\"-> JsString(x._1." + x.name + ")"
        }
    }

    def generateMap(entity:SpecEntity) = {
        val mainObj = "\"" + entity.name + "\" -> Json.toJson(x._1)";
        val attr = entity.attributes.map(x => {
            this.getMapField(x)
        }).mkString(", ")
        val relationObjs = entity.relations.zipWithIndex.map{ case (r,i) => {
            "\"" + r.of + "\" -> Json.toJson(x._" + (i + 2) + ")"
        }}
        "Map( " + attr + ", " + relationObjs.mkString(", ") + ")"
    }

    def generateMainController = {
        val injects = all.map(entity => {
            "    " + entity.plural.toLowerCase + ":Generated" + entity.plural.capitalize + "Controller"
        }).mkString(",\n")
        val maps = all.map(entity => {
            "        \"" + entity.plural + "\" -> " + entity.plural
        }).mkString(",\n")
        val entityTypes = all.map(entity => {
            "        \"" + entity.name + "\" -> \"" + entity.plural + "\""
        }).mkString(",\n")
        tplController.replaceAll("\\{controllers\\}", injects)
                     .replaceAll("\\{controllermap\\}", maps)
                     .replaceAll("\\{entityTypes\\}", entityTypes)
    }
}