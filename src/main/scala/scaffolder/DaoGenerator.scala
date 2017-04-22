package scaffolder
import scaffolder._

case class DaoGenerator(spec:SpecEntity, all:List[SpecEntity]) {

    val daoStr:String = """
                          |
                          |@Singleton
                          |class {pluralWC} @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
                          |
                          |  val {plural} = TableQuery[{nameWC}TableDef]
                          |  {relations} 
                          |  val insertQuery = {plural} returning {plural}.map(_.id) into (({name}, id) => {name}.copy(id = id))
                          |
                          |  def insert({name}:{nameWC}) = dbConfig.db.run(insertQuery += {name}
                          |    
                          |  def update({name}:{nameWC}) = dbConfig.db.run {
                          |    {plural}.filter(_.id === {name}.id).update({name})
                          |  }
                          |
                          |  def delete(id:Long) = dbConfig.db.run {
                          |    {plural}.filter(_.id === id).delete
                          |  }
                          |
                          |  def getById(id:Long) = dbConfig.db.run {
                          |    {getById}
                          |  }
                          |
                          |  def getAll = dbConfig.db.run {
                          |    {getall}
                          |  }
                          |
                          |}
                          |""".stripMargin

    
    
    def generate = {
        val withPlural = daoStr.replaceAll("\\{plural\\}", spec.plural)
        val pluralLower = withPlural.replaceAll("\\{pluralWC\\}", spec.plural.capitalize)
        val withName = pluralLower.replaceAll("\\{name\\}", spec.name)
        val nameLower = withName.replaceAll("\\{nameWC\\}", spec.name.capitalize)
        val relations = nameLower.replaceAll("\\{relations\\}", getRelations.mkString("\n"))
        val byid = relations.replaceAll("\\{getById\\}", generateGetById)
        val getall = byid.replaceAll("\\{getall\\}", generateGetAll)
        getall
    }
    
    def generateSelect(afterRelation:String, after:String) = {
        if(spec.relations.length > 0) {
            val relations = spec.relations.map(x => {
                val entity = all.filter(e => e.name == x.of)
                if(entity.length > 0) {
                    ".join(" + entity.head.plural + ").on(_."+entity.head.name+"_id === _.id)"
                } else {
                    println("Warning: Unknown relation used: " + x.of)
                    ""
                }
            })
            spec.plural + relations.mkString + afterRelation
        } else {
            spec.plural + after
        }
    }

    def generateGetById = generateSelect(".filter(_._1.id === id).headOption", ".filter(_.id === id).headOption")
    def generateGetAll = generateSelect(".result", ".result")

    def generateGetByIdOld = {
        if(spec.relations.length > 0) {
            val relations = spec.relations.map(x => {
                val entity = all.filter(e => e.name == x.of)
                if(entity.length > 0) {
                    ".join(" + entity.head.plural + ").on(_."+entity.head.name+"_id === _.id)"
                } else {
                    println("Warning: Unknown relation used: " + x.of)
                    ""
                }
            })
            spec.plural + relations.mkString + ".filter(_._1.id === id).headOption"
        } else {
            spec.plural + ".filter(_.id === id).headOption"
        }
    }

    def getRelations = {
        spec.relations.filter(x => x.has == "one").map(relation => {
            val r = all.filter(entity => entity.name == relation.of)
            if(r.length > 0) {
                val re = r.head
                s"val " + re.plural.toLowerCase + " = TableQuery["+re.name.toLowerCase.capitalize+"TableDef]"
            } else {
                println("Warning: Unknown relation used: " + relation.of)
                ""
            }
        })
    }
}