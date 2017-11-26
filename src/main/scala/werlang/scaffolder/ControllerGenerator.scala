package werlang.scaffolder
import werlang.scaffolder._


case class ControllerGenerator(all:List[SpecEntity], relations:List[SpecEntity]) {

    val traittpl:String = """|
                        |trait TGenController {
                        |    def getAll(request:AuthRequest[AnyContent]):Future[Result]
                        |    def insert(request:AuthRequest[JsValue]):Future[Result]
                        |    def update(request:AuthRequest[JsValue]):Future[Result]
                        |    def delete(id:Long, request:AuthRequest[AnyContent]):Future[Result]
                        |    def createNew(request:AuthRequest[AnyContent]):Future[Result]
                        |    def getFormById(id:Long, request:AuthRequest[AnyContent]):Future[Result]
                        |}
                        |""".stripMargin

    val relationtraittpl = """
                        |trait TGenRelationController {
                        |    def link(source_id:Long, target_id:Long):Future[Result]
                        |    def unlink(source_id:Long, target_id:Long):Future[Result]
                        |    def getBySourceId(id:Long):Future[Result]
                        |    def getAll:Future[Result]
                        |}
                        |""".stripMargin 

    var relationtpl:String = """
                        |@Singleton
                        |class Generated{pluralWC}Controller @Inject() (
                        |    WithAuthAction:AuthAction,
                        |    {plural}:{pluralWC}
                        |    ) extends Controller with TGenRelationController {
                        |    
                        |    implicit val {nameWC}Writes = Json.writes[{nameWC}]
                        |
                        |    def link(source_id:Long, target_id:Long) = {
                        |        {plural}.link({nameWC}(source_id, target_id)).map(x => {
                        |            Ok(Json.toJson(Map("success" -> JsBoolean(true))))
                        |        })
                        |    }
                        |
                        |    def unlink(source_id:Long, target_id:Long) = {
                        |        {plural}.unlink({nameWC}(source_id, target_id)).map(x => {
                        |            Ok(Json.toJson(Map("success" -> JsBoolean(true))))
                        |        })
                        |    }
                        |
                        |    def getBySourceId(source_id:Long) = {
                        |        {plural}.getBySourceId(source_id).map(x => {
                        |            Ok(Json.toJson(x))
                        |        })
                        |    }
                        |
                        |    def getAll = {
                        |      {plural}.getAll.map(x => {
                        |        Ok(Json.toJson(x))
                        |      })
                        |    }
                        |
                        |}
                        |
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
                    |    def update(request:AuthRequest[JsValue]) = {
                    |        (request.body \ "entity").asOpt[{nameWC}].map( entity => {
                    |             {plural}.update(entity).map(x => {
                    |                 Ok(Json.toJson(Map("success" -> JsBoolean(true))))
                    |             })
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
                    |     def getFormById(id:Long, request:AuthRequest[AnyContent]) = {
                    |        {plural}.getById(id).map(x => x match {
                    |            case Some(p) => {
                    |                Ok(Json.toJson(
                    |                    Map(
                    |                       "attributes" -> List(
                    |{formfields}
                    |                       ),
                    |                       "relations" -> List(
                    |{relationfields}
                    |                       )
                    |                    )
                    |                ))
                    |            }
                    |            case None => BadRequest("Invalid {plural} id provided.")
                    |        })
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
                        |    val relations = Map(
                        |{relationsMap}
                        |    )
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
                        |    def update(name:String) = WithAuthAction.async(parse.json) { request =>
                        |        controllers.get(name) match {
                        |            case Some(x) => x.update(request)
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
                        |    
                        |    def getFormById(name:String, id:Long) = WithAuthAction.async { request =>
                        |        controllers.get(name) match {
                        |            case Some(x) => x.getFormById(id, request)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }
                        |    }
                        |
                        |    def link(name:String, source_id:Long, target_id:Long) = WithAuthAction.async { request =>
                        |        relations.get(name) match {
                        |            case Some(x) => x.link(source_id, target_id)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }    
                        |    }
                        |
                        |    def unlink(name:String, source_id:Long, target_id:Long) = WithAuthAction.async { request =>
                        |        relations.get(name) match {
                        |            case Some(x) => x.unlink(source_id, target_id)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }
                        |    }
                        |
                        |    def getRelationsBySourceId(name:String, source_id:Long) = WithAuthAction.async { request =>
                        |        relations.get(name) match {
                        |            case Some(x) => x.getBySourceId(source_id)
                        |            case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |        }    
                        |    }
                        |
                        |    def getAllRelations(name:String) = WithAuthAction.async { request => 
                        |      relations.get(name) match {
                        |        case Some(x) => x.getAll
                        |        case None => Future(BadRequest("Error: Entity with name " + name + " doesn't exist."))
                        |      }
                        |    }
                        |
                        |
                        |}
                        |""".stripMargin

    def generate = {
        traittpl + all.map(entity => {
            //val getstr = {
            //    if(entity.relations.length == 0) {
            //        entity.plural + ".getAll.map(x => Ok(Json.toJson(x)))"
            //    } else {
            //        entity.plural + ".getAll.map(entities => Ok(Json.toJson(entities.map(x => "+ generateMap(entity) +" ))))"
            //   }
            //}
            val getstr = entity.plural + ".getAll.map(x => Ok(Json.toJson(x)))";
            tpl.replaceAll("\\{name\\}", entity.name.toLowerCase)
               .replaceAll("\\{nameWC\\}", entity.name.toLowerCase.capitalize)
               .replaceAll("\\{plural\\}", entity.plural.toLowerCase)
               .replaceAll("\\{pluralWC\\}", entity.plural.toLowerCase.capitalize)
               .replaceAll("\\{getstr\\}", getstr)
               .replaceAll("\\{relationReads\\}", generateReads(entity))
               .replaceAll("\\{initialValues\\}", generateInitialValues(entity.attributes))
               .replaceAll("\\{formfields\\}", generateFormFields(entity))
               .replaceAll("\\{relationfields\\}", generateRelationFields(entity))

        }).mkString("\n") + generateRelations + generateMainController
    }

    def generateRelations = {
        relationtraittpl + relations.map(entity => {
            relationtpl.replaceAll("\\{name\\}", entity.name.toLowerCase)
                       .replaceAll("\\{nameWC\\}", entity.name.toLowerCase.capitalize)
                       .replaceAll("\\{plural\\}", entity.plural.toLowerCase)
                       .replaceAll("\\{pluralWC\\}", entity.plural.toLowerCase.capitalize)
        }).mkString("\n")
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

    def generateRelationFields(entity:SpecEntity) = {
        entity.relations.filter(_.has == "many").map(r => {
            val relationname = entity.name + r.of
            "                           Map(\"relationname\" -> JsString(\"" + relationname + "\"), \"relation\" -> JsString(\"" + r.of + "\"), \"unique\" -> JsBoolean("+r.unique+"))"
        }).mkString(",\n")
    }

    def generateFormFields(entity:SpecEntity) = {
        
        val relations = entity.relations.filter(_.has == "one").map(r => {
            EntityAttribute(r.of + "_id", "relation", Some(r.of), None)
        })

        val attributes:List[EntityAttribute] = entity.attributes.filter(x => relations.filter(y => y.name == x.name).length == 0) ++ relations
        attributes.map(a => {
            val attrType = a.atype.toLowerCase
            val longs = List("long", "key", "relation")
            val fieldValue = {
                if(longs.contains(attrType)) s"JsNumber(p.${a.name})"
                else if(attrType == "timestamp") s"JsNumber(p.${a.name}.getTime())"
                else s"JsString(p.${a.name})"
            }

            val fieldType = {
                if(a.name.endsWith("_id") && attrType != "relation") "readonly"
                else if(attrType == "key") "readonly"
                else if(attrType == "long") "number"
                else if(attrType == "timestamp") "datetime"
                else if(attrType == "string") "text"
                else if(attrType == "text") "textarea"
                else attrType
            }
            val relation = {
                if(attrType == "relation") {
                    val r = entity.relations.filter(_.of == a.entity.getOrElse(""))
                    val isUnique = r.head.unique
                    "\"relation\" -> JsString(\"" + a.entity.getOrElse("") + "\"),\"unique\" -> JsBoolean("+isUnique+"),"
                }
                else ""
            }
            "                           Map(\"name\" -> JsString(\"" + a.name + "\"), "+relation+" \"type\" -> JsString(\"" + fieldType + "\"), \"value\" -> " + fieldValue + ")"
        }).mkString(",\n")
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
        val injects = (all ++ relations).map(entity => {
            "    " + entity.plural.toLowerCase + ":Generated" + entity.plural.capitalize + "Controller"
        }).mkString(",\n")
        val maps = all.map(entity => {
            "        \"" + entity.name + "\" -> " + entity.plural
        }).mkString(",\n")
        val entityTypes = all.map(entity => {
            "        \"" + entity.name + "\" -> \"" + entity.plural + "\""
        }).mkString(",\n")
        val relationsMap = relations.map(r => {
            "        \"" + r.name + "\" -> " + r.plural
        }).mkString(",\n")
        tplController.replaceAll("\\{controllers\\}", injects)
                     .replaceAll("\\{controllermap\\}", maps)
                     .replaceAll("\\{entityTypes\\}", entityTypes)
                     .replaceAll("\\{relationsMap\\}", relationsMap)
    }
}