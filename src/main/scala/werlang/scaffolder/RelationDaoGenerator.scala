package werlang.scaffolder
import werlang.scaffolder._
import werlang.spec._

case class RelationDaoGenerator(spec:SpecEntity) {

    val daoStr:String = """
                          |
                          |trait T{pluralWC} extends HasDatabaseConfigProvider[JdbcProfile] {
                          |
                          |  val {plural} = TableQuery[{nameWC}TableDef]
                          |
                          |  def link({name}:{nameWC}) = dbConfig.db.run({plural} += {name})
                          |  
                          |  def unlink({name}:{nameWC}) = dbConfig.db.run {
                          |    {plural}.filter(x => x.source_id === {name}.source_id && x.target_id === {name}.target_id).delete
                          |  }
                          |
                          |  def getBySourceId(id:Long) = dbConfig.db.run {
                          |    {plural}.filter(_.source_id === id).result
                          |  }
                          |
                          |  def getAll = dbConfig.db.run {  
                          |    {plural}.result
                          |  }
                          |
                          |}
                          |""".stripMargin

    
    
    def generate = {
        daoStr.replaceAll("\\{plural\\}", spec.plural)
              .replaceAll("\\{pluralWC\\}", spec.plural.capitalize)
              .replaceAll("\\{name\\}", spec.name)
              .replaceAll("\\{nameWC\\}", spec.name.capitalize)
    }

    def generateClass= {
        val classtpl = """|@Singleton
                          |class {pluralWC} @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends T{pluralWC} {
                          |
                          |}""".stripMargin
        
        classtpl.replaceAll("\\{pluralWC\\}", spec.plural.capitalize)
    }
    

}