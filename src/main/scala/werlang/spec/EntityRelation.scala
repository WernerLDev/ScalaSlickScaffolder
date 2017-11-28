package werlang.spec

case class EntityRelation(has:String, of:String, unique:Boolean) {
    
    override def toString = {
        s"      { Has $has of $of, unique: $unique }"
    }
    
    def toSql = {

    }
}