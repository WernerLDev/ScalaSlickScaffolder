package scaffolder

import scaffolder._

case class Generator(spec:SpecEntity) {

    val modelGenerator:ModelGenerator = ModelGenerator(spec)
    val daoGenerator:DaoGenerator = DaoGenerator(spec)

    def generate = {
        val m = modelGenerator.generate
        val dao = daoGenerator.generate

        m + dao
    }

}