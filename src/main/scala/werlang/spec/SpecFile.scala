package werlang.spec

case class SpecFile(
    packageName:String, 
    modelFolder:String, 
    migrationFile:String, 
    controllerPackage:String, 
    controllerFile:String,  
    entities:List[SpecEntity]
) {
    override def toString = {
        entities.map(_.toString).mkString("\n")
    }
}