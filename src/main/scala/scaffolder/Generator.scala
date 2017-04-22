package scaffolder

import scaffolder._

case class Generator(spec:SpecEntity, all:List[SpecEntity]) {

    val modelGenerator:ModelGenerator = ModelGenerator(spec)
    val daoGenerator:DaoGenerator = DaoGenerator(spec, all)

    def generate = {
        val m = modelGenerator.generate
        val dao = daoGenerator.generate

        m + dao
    }

}