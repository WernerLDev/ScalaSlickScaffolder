package werlang.spec

import werlang.spec._
import werlang._

object CheckSpec {

    val validAttributes = List("key", "string", "text", "long", "date", "datetime", "relation")

    val validHas = List("one", "many")

    def check(entities:List[SpecEntity]):Boolean = {
        !entities.exists(entity => {
            val attrCheck = entity.attributes
              .filter(a => {
                  val isInvalid = !validAttributes.contains(a.atype)
                  if(isInvalid) {
                      Message.fail(s"Entity '${entity.name}' has an invalid attribute called '${a.atype}'")
                  }
                  isInvalid
              })
              
              val relationCheck = entity.relations
                .filter(r => {
                    val isInvalid = !validHas.contains(r.has)
                    if(isInvalid) {
                        Message.fail(s"Entity '${entity.name}' has an invalid relationtype '${r.has}'")
                    }
                    val invalidRelation = !entities.exists(x => x.name == r.of)
                    if(invalidRelation) {
                        Message.fail(s"Entity '${entity.name}' has an invalid relation '${r.of}'")
                    }
                    isInvalid || invalidRelation
                })

              attrCheck.nonEmpty || relationCheck.nonEmpty
        })
    }

}