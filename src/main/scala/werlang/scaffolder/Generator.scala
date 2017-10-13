package werlang.scaffolder

import werlang.scaffolder._

case class Generator(spec:SpecEntity, all:List[SpecEntity]) {

    val modelGenerator:ModelGenerator = ModelGenerator(spec)
    val daoGenerator:DaoGenerator = DaoGenerator(spec, all)
    val relationDao:RelationDaoGenerator = RelationDaoGenerator(spec)
    

    def generate = {
        val NtoN = spec.relations.filter(_.has == "many")
        //NtoN.map(x => spec.name + x.of).foreach(println)


        val m = modelGenerator.generate
        val dao = daoGenerator.generate

        m + dao
    }

    def generateRelationEntity = {
        val m = modelGenerator.generate
        val dao = relationDao.generate
        m + dao
    }

    def generateClass = {
        daoGenerator.generateClass
    }

}