package werlang.scaffolder
import werlang.scaffolder._

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
                        |{tblfields},
                        |  PRIMARY KEY (`id`),
                        |  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
                        |);""".stripMargin

    val upTplRelation:String = """
                        |CREATE TABLE `{tblname}` (
                        |{tblfields},
                        |  PRIMARY KEY (`source_id`,`target_id`),
                        |  CONSTRAINT `{tblname}_{source}_FK` FOREIGN KEY (`source_id`) REFERENCES `{source}` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                        |  CONSTRAINT `{tblname}_{target}_FK` FOREIGN KEY (`target_id`) REFERENCES `{target}` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
                        |);
                        |""".stripMargin

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
        else if(atype == "long") "INT NOT NULL"
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
            //val createsql = upTpl.replaceAll("\\{tblname\\}", entity.plural)
            val createsql = {
                if(entity.attributes.filter(_.name == "id").length > 0) {
                    upTpl.replaceAll("\\{tblname\\}", entity.plural)
                } else {
                    upTplRelation.replaceAll("\\{tblname\\}", entity.plural)
                                 .replaceAll("\\{source\\}", entity.relations.filter(_.has == "source").head.of)
                                 .replaceAll("\\{target\\}", entity.relations.filter(_.has == "target").head.of)
                }
            }
            val fields = entity.attributes.map(field => {
                if(field.atype == "key") {
                    "  `" + field.name + "` INT NOT NULL AUTO_INCREMENT" 
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
        val allNonRelations = all.filter(x => x.attributes.filter(_.name == "id").length > 0)
        val allRelations = all.filter(x => x.attributes.filter(_.name == "id").length <= 0)
        (allRelations ++ allNonRelations).map(entity => {
            "DROP TABLE `" + entity.plural + "`;"
        })
    }
}