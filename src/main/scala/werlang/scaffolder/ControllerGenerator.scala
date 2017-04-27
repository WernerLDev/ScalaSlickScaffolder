package werlang.scaffolder
import werlang.scaffolder._

case class ControllerGenerator(all:List[SpecEntity]) {

    val traittpl:String = """|
                        |trait TGenController {
                        |    def getAll(request:AuthRequest[AnyContent]):Future[Result]
                        |    def insert(request:AuthRequest[JsValue]):Future[Result]
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
                    |}
                    |""".stripMargin


    val tplController = """|
                        |@Singleton
                        |class GeneratedController @Inject() (
                        |    WithAuthAction:AuthAction,
                        |    {controllers}
                        |) extends Controller {
                        |
                        |
                        |    val controllers = Map(
                        |        {controllermap}
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
                        |    def getEntities = WithAuthAction { request =>
                        |        val entities = controllers.map { case (k,v) => {
                        |            Json.toJson(Map("name" -> JsString(k)))
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
                    entity.plural + ".getAll.map(x => Ok(Json.toJson(x.map(_._1))))"
                }
            }
            tpl.replaceAll("\\{name\\}", entity.name.toLowerCase)
               .replaceAll("\\{nameWC\\}", entity.name.toLowerCase.capitalize)
               .replaceAll("\\{plural\\}", entity.plural.toLowerCase)
               .replaceAll("\\{pluralWC\\}", entity.plural.toLowerCase.capitalize)
               .replaceAll("\\{getstr\\}", getstr)
        }).mkString("\n") + generateMainController
    }

    def generateMainController = {
        val injects = all.map(entity => {
            entity.plural.toLowerCase + ":Generated" + entity.plural.capitalize + "Controller"
        }).mkString(",\n")
        val maps = all.map(entity => {
            "\"" + entity.plural + "\" -> " + entity.plural
        }).mkString(",\n")
        tplController.replaceAll("\\{controllers\\}", injects)
                     . replaceAll("\\{controllermap\\}", maps)
    }
}