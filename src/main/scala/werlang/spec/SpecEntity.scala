package werlang.spec

case class SpecEntity(
    name:String, 
    plural:String, 
    attributes:List[EntityAttribute], 
    relations:List[EntityRelation],
    isAbstract:Option[Boolean],
    inherits:Option[String]
) {
    
    override def toString = {

        val r = {
            if(relations.length == 0) "   Relations: []"
            else {
                s"""|   Relations: [
                    |${relations.map(_.toString).mkString(",\n")}
                    |   ]""".stripMargin
            }
        }

        s"""| { 
            |   Name: $name, 
            |   Plural: $plural,
            |   Attributes: [${attributes.map(_.toString).mkString(",")}],
            |$r
            | }
        """.stripMargin
    }
    
    def toSql = {

    }
}