package scaffolder
import scaffolder._

case class MigrationGenerator(all:List[SpecEntity]) {

    val tpl:String = """
                    |# Code generated schema
                    |
                    |# --- !Ups
                    |{upsql}
                    |
                    |{indexes}
                    |
                    |# --- !Downs
                    |{downsql}
                    |""".stripMargin

    val upTpl:String = """
                        |CREATE TABLE `{tblname}` ( 
                        |
                        |{tblfields},
                        |  PRIMARY KEY (`id`),
                        |  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
                        |);""".stripMargin


    def generate = {
        val fkeys = all.map(entity => {
            generateIndexes(entity)
        })
        tpl.replaceAll("\\{downsql\\}", generateDowns.mkString("\n"))
           .replaceAll("\\{upsql\\}", generateUps.mkString("\n"))
           .replaceAll("\\{indexes\\}", fkeys.mkString("\n"))
    }

    def getSqlType(atype:String) = {
        if(atype == "string") "VARCHAR(255) NOT NULL"
        else if(atype == "long") "INT(11) NOT NULL"
        else if(atype == "text") "TEXT NOT NULL"
        else if(atype == "timestamp") "DATETIME NOT NULL DEFAULT NOW()"
        else ""
    }

    def generateIndexes(entity:SpecEntity) = {
        val keys = entity.relations.filter(x => x.has == "one").map(relation => {
            val re = all.filter(x => x.name == relation.of)
            if(re.length > 0) {
                "ALTER TABLE `"+entity.plural+"` ADD INDEX ("+re.head.name+"_id)"
            } else {
                ""
            }
        }).mkString(";\n")
        if(keys.length > 0) keys + ";" else ""
    }

    def generateUps = {
        all.map(entity => {
            val createsql = upTpl.replaceAll("\\{tblname\\}", entity.plural)
            val fields = entity.attributes.map(field => {
                if(field.atype == "key") {
                    "  `" + field.name + "` BIGINT NOT NULL AUTO_INCREMENT" 
                } else {
                    "  `" + field.name + "` " + getSqlType(field.atype.toLowerCase)
                }
            }).mkString(",\n")
            //val keys = generateForeignKeys(entity)
            //val out = (fields ++ keys).mkString(",\n")
            createsql.replaceAll("\\{tblfields\\}", fields)
        })
    }

    def generateDowns = {
        all.map(entity => {
            "DROP TABLE `" + entity.plural + "`;"
        })
    }
}