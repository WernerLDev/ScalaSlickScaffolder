package scaffolder
import scaffolder._

case class MigrationGenerator(all:List[SpecEntity]) {

    val tpl:String = """
                    |# Code generated schema
                    |
                    |# --- !Ups
                    |{upsql}
                    |
                    |# --- !Downs
                    |{downsql}
                     """.stripMargin

    val upTpl:String = """
                        |CREATE TABLE `{tblname}` ( 
                        |
                        |{tblfields}, 
                        |
                        |  PRIMARY KEY (`id`),
                        |  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
                        |);""".stripMargin


    def generate = {
        val migration = tpl.replaceAll("\\{downsql\\}", generateDowns.mkString("\n"))
        val withUps = migration.replaceAll("\\{upsql\\}", generateUps.mkString("\n"))
        withUps
    }

    def generateUps = {
        all.map(entity => {
            val createsql = upTpl.replaceAll("\\{tblname\\}", entity.plural)
            val fields = entity.attributes.map(field => {
                if(field.atype == "key") {
                    "  `" + field.name + "` BIGINT NOT NULL AUTO_INCREMENT" 
                } else {
                    "  `" + field.name + "` " + field.atype + " NULL"
                }
            }).mkString(",\n")
            createsql.replaceAll("\\{tblfields\\}", fields)
        })
    }

    def generateDowns = {
        all.map(entity => {
            "DROP TABLE `" + entity.plural + "`;"
        })
    }
}