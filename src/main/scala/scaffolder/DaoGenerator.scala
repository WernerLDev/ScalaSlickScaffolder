package scaffolder
import scaffolder._

case class DaoGenerator(spec:SpecEntity) {

    val daoStr:String = """
                          |
                          |@Singleton
                          |class {pluralWC} @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
                          |
                          |  val {plural} = TableQuery[{nameWC}TableDef]
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
                          |  def getById(id:Long):Future[Option[{nameWC}]] = dbConfig.db.run {
                          |    {plural}.filter(_.id === id).headOption
                          |  }
                          |
                          |  def getAll:Future[Seq[{nameWC}]] = dbConfig.db.run {
                          |    {plural}.result
                          |  }
                          |
                          |}
                          |""".stripMargin

    
    
    def generate = {
        val withPlural = daoStr.replaceAll("\\{plural\\}", spec.plural)
        val pluralLower = withPlural.replaceAll("\\{pluralWC\\}", spec.plural.capitalize)
        val withName = pluralLower.replaceAll("\\{name\\}", spec.name)
        val nameLower = withName.replaceAll("\\{nameWC\\}", spec.name.capitalize)
        nameLower
    }
}