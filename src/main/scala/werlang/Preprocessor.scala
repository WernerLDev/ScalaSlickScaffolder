package werlang

import werlang.scaffolder._
import play.api.libs.json._
import java.io._
import werlang.spec._



object Preprocessor {
    
    def addIds(obj:SpecEntity):SpecEntity = {
        val idAttr = EntityAttribute("id", "key", None, None)
        val nameAttr = EntityAttribute("name", "string", None, None)
        val newAttributes = idAttr :: obj.attributes
        val relations = obj.relations.filter(x => x.has == "one").map(relation => {
            EntityAttribute(relation.of.toLowerCase + "_id", "long", None, None)
        })
        obj.copy(attributes = newAttributes ++ relations)
    }

    def forceLowerCase(obj:SpecEntity):SpecEntity = {
        val attributes = obj.attributes.map(attr => attr.copy(name = attr.name.toLowerCase))
        val relations = obj.relations.map(r => r.copy(of = r.of.toLowerCase))
        obj.copy(name = obj.name.toLowerCase, plural = obj.plural.toLowerCase, attributes = attributes, relations = relations)
    }

    def createRelationEntities(entity:SpecEntity, all:List[SpecEntity]):List[SpecEntity] = {

        entity.relations.filter(_.has == "many").map(x => {
            val relatedEntity = all.filter(_.name == x.of)
            if(relatedEntity.nonEmpty) {
                SpecEntity(
                    entity.name + x.of,
                    entity.name + relatedEntity.head.plural,
                    List(EntityAttribute("source_id", "Long", None, None), EntityAttribute("target_id", "Long", None, None)),
                    List(
                        EntityRelation("source", entity.plural.toLowerCase, false),
                        EntityRelation("target", relatedEntity.head.plural.toLowerCase, x.unique)
                    )
                )
            } else {
                println("Error: Found relation with invalid entity: " + x.of)
                SpecEntity("-", "-", List(), List())
            }
        }).filter(_.name != "-")
    }

}