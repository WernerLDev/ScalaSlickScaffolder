# ScalaSlickScaffolder
Generates models for slick

Still work in progress. The spec file used is at this moment hardcoded in main.scala and the output is written to the output folder.

The following spec file written as json will be turned into scala code.

```json
{
    "packageName" : "models",
    "entities" : [
        {
            "name" : "post",
            "plural" : "posts",
            "attributes" : [
                { "name" : "title", "atype" : "String"},
                { "name" : "content", "atype" : "text" },
                { "name" : "category_id", "atype" : "Long" }
            ]
        }, {
            "name" : "Category",
            "plural" : "Categories",
            "attributes" : [
                { "name" : "name", "atype" : "string"}
            ]
        }
    ]
}
```

Result:
```Scala
package models

import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Singleton
import javax.inject._
import play.api.Play.current
import java.sql.Timestamp
import slick.profile.SqlProfile.ColumnOption.SqlType



case class Post (id:Long, title:String, content:String, category_id:Long)

class PostTableDef(tag:Tag) extends Table[Post](tag, "Posts") {
  
  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def title = column[String]("title")
  def content = column[String]("content")
  def category_id = column[Long]("category_id")

  override def * = (id, title, content, category_id) <>(Post.tupled, Post.unapply)
}


@Singleton
class Posts @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val posts = TableQuery[PostTableDef]
  val insertQuery = posts returning posts.map(_.id) into ((post, id) => post.copy(id = id))

  def insert(post:Post) = dbConfig.db.run(insertQuery += post
    
  def update(post:Post) = dbConfig.db.run {
    posts.filter(_.id === post.id).update(post)
  }

  def delete(id:Long) = dbConfig.db.run {
    posts.filter(_.id === id).delete
  }

  def getById(id:Long):Future[Option[Post]] = dbConfig.db.run {
    posts.filter(_.id === id).headOption
  }

  def getAll:Future[Seq[Post]] = dbConfig.db.run {
    posts.result
  }

}



case class Category (id:Long, name:String)

class CategoryTableDef(tag:Tag) extends Table[Category](tag, "Categories") {
  
  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def name = column[String]("name")

  override def * = (id, name) <>(Category.tupled, Category.unapply)
}


@Singleton
class Categories @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val categories = TableQuery[CategoryTableDef]
  val insertQuery = categories returning categories.map(_.id) into ((category, id) => category.copy(id = id))

  def insert(category:Category) = dbConfig.db.run(insertQuery += category
    
  def update(category:Category) = dbConfig.db.run {
    categories.filter(_.id === category.id).update(category)
  }

  def delete(id:Long) = dbConfig.db.run {
    categories.filter(_.id === id).delete
  }

  def getById(id:Long):Future[Option[Category]] = dbConfig.db.run {
    categories.filter(_.id === id).headOption
  }

  def getAll:Future[Seq[Category]] = dbConfig.db.run {
    categories.result
  }

}


```
