package werlang.generator

import java.io._

import werlang.spec._
import werlang._
import werlang.scaffolder._


case class CodeGenerator(spec:SpecFile, relationEntities:List[SpecEntity]) {

  val imports:String = ImportGenerator(spec).generate
  val importsController:String = ImportGenerator(spec).generateControllerImport

  def generate(): Unit = {
    val pw = new PrintWriter(new File(spec.modelFolder + "/Tables.scala"))
    pw.write(imports)
    pw.write(generateRelationModels(relationEntities))
    pw.write(generateModels(spec.entities))
    pw.close()
    Message.ok(s"Generated Tables.scala")

    generateDaoClasses(spec.entities ++ relationEntities)
    generateMigrations(spec.entities ++ relationEntities)
    generateControllers(spec.entities)
  }

  def generateModels(entities:List[SpecEntity]): String = {
    entities.map(entity => {
      val modelGenerator:ModelGenerator = ModelGenerator(entity)
      val daoGenerator:DaoGenerator = DaoGenerator(entity, spec.entities ++ relationEntities)

      val m = modelGenerator.generate
      val dao = daoGenerator.generate

      m + dao
    }).mkString("\n")
  }

  def generateRelationModels(entities:List[SpecEntity]):String = {
    entities.map(entity => {
      val modelGenerator:ModelGenerator = ModelGenerator(entity)
      val daoGenerator:RelationDaoGenerator = RelationDaoGenerator(entity)

      val m = modelGenerator.generate
      val dao = daoGenerator.generate

      m + dao
    }).mkString("\n")
  }


  def generateMigrations(entities:List[SpecEntity]): Unit = {
    val migrations = MigrationGenerator(entities)
    val pw2 = new PrintWriter(new File(spec.migrationFile))
    pw2.write(migrations.generate)
    pw2.close()
    Message.ok(s"Generated ${spec.migrationFile}")
  }

  def generateControllers(entities:List[SpecEntity]): Unit = {
    val controllers = ControllerGenerator(entities, relationEntities)
    val pw3 = new PrintWriter(new File(spec.controllerFile))
    pw3.write(importsController)
    pw3.write(controllers.generate)
    pw3.close()
    Message.ok(s"Generated ${spec.controllerFile}")
  }

  def generateDaoClasses(entities:List[SpecEntity]): Unit = {
    entities.foreach(entity => {
      val generator = DaoGenerator(entity, List())
      val modelFile = new File(spec.modelFolder + "/" + entity.name + ".scala")
      if(modelFile.exists) {
        Message.ok(s"Skipping ${entity.name}.scala, Already exists.")
      } else {
        val classpw = new PrintWriter(modelFile)
        classpw.write(imports)
        classpw.write(generator.generateClass)
        classpw.close()
        Message.ok(s"Generated ${entity.name}.scala")
      }
    })
  }

}