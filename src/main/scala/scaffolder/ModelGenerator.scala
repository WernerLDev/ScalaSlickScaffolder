package scaffolder
import scaffolder._

case class ModelGenerator(spec:SpecEntity) {

    val modelStr:String = """|case class {name} ({modelAttributes})
                          |
                          |class {name}TableDef(tag:Tag) extends Table[{name}](tag, "{plural}") {
                          |  
                          |{defAttributes}
                          |
                          |  override def * = ({attributes}) <>({name}.tupled, {name}.unapply)
                          |}
                          |""".stripMargin

    def generate = {
        val modelName = modelStr.replaceAll("\\{name\\}", spec.name.capitalize)
        val modelPlural = modelName.replaceAll("\\{plural\\}", spec.plural.capitalize)
        val modelAttr = modelPlural.replaceAll("\\{modelAttributes\\}", getModelAttributes(spec.attributes))
        val attr = modelAttr.replaceAll("\\{attributes\\}", getAttributes(spec.attributes))
        val defAttr = attr.replaceAll("\\{defAttributes\\}", getDefAttributes(spec.attributes))
        defAttr
    }

    def getScalaType(t:String) = {
        if(t == "text") "String"
        else if(t == "string") "String"
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

