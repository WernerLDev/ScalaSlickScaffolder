package werlang.spec

case class EntityAttribute(name:String, atype:String, entity:Option[String], options:Option[List[String]]) {
    override def toString:String = {
        s" $name:$atype "
    }
    
    def toSql = {

    }
}