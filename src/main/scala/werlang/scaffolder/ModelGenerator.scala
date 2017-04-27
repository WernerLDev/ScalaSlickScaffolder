package werlang.scaffolder
import werlang.scaffolder._

case class ModelGenerator(spec:SpecEntity) {

    val modelStr:String = """|case class {nameWC} ({modelAttributes}) 
                          |
                          |class {nameWC}TableDef(tag:Tag) extends Table[{nameWC}](tag, "{plural}") {
                          |  
                          |{defAttributes}
                          |
                          |  override def * = ({attributes}) <>({nameWC}.tupled, {nameWC}.unapply)
                          |}
                          |""".stripMargin

    def generate = {
        modelStr.replaceAll("\\{nameWC\\}", spec.name.capitalize)
                .replaceAll("\\{plural\\}", spec.plural.toLowerCase)
                .replaceAll("\\{modelAttributes\\}", getModelAttributes(spec.attributes))
                .replaceAll("\\{attributes\\}", getAttributes(spec.attributes))
                .replaceAll("\\{defAttributes\\}", getDefAttributes(spec.attributes))
    }

    def getScalaType(t:String) = {
        if(t == "text") "String"
        else if(t == "string") "String"
        else if(t == "timestamp") "Timestamp"
        else if(t == "key") "Long"
        else t
    }

    def getModelAttributes(attributes:List[EntityAttribute]) = {
        
        attributes.map(x => {
            x.name + ":" + getScalaType(x.atype)
        }).mkString(", ")
    }

    def getAttributes(attributes:List[EntityAttribute]) = {
        attributes.map(x => x.name).mkString(", ")
    }

    def getDefAttributes(attributes:List[EntityAttribute]) = {
        val sample = "  def {name} = column[{type}]({defname})"
        attributes.map(x => {
            val name = x.name
            val defName = {
                if(x.atype == "key") s""""$name", O.PrimaryKey,O.AutoInc"""
                else s""""$name""""
            }
            sample.replaceAll("\\{name\\}", x.name)
                  .replaceAll("\\{defname\\}", defName)
                  .replaceAll("\\{type\\}", getScalaType(x.atype))
        }).mkString("\n")
    }
}

