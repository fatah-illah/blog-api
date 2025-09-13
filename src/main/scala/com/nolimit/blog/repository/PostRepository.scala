package com.nolimit.blog.repository

import com.nolimit.blog.domain.{Post, PostId, UserId}
import slick.jdbc.PostgresProfile.api._
import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class PostRepository(db: Database) {
  // Custom column types untuk UUID dan Instant
  implicit val userIdColumnType = MappedColumnType.base[UserId, UUID](
    userId => userId.value,
    uuid => UserId(uuid)
  )
  
  implicit val postIdColumnType = MappedColumnType.base[PostId, UUID](
    postId => postId.value,
    uuid => PostId(uuid)
  )
  
  implicit val instantColumnType = MappedColumnType.base[Instant, java.sql.Timestamp](
    instant => java.sql.Timestamp.from(instant),
    timestamp => timestamp.toInstant
  )

  class Posts(tag: Tag) extends Table[Post](tag, "posts") {
    def id = column[PostId]("id", O.PrimaryKey)
    def content = column[String]("content")
    def createdAt = column[Instant]("created_at")
    def updatedAt = column[Instant]("updated_at")
    def authorId = column[UserId]("author_id")
    
    def * = (id, content, createdAt, updatedAt, authorId) <> (Post.tupled, Post.unapply)
  }

  val posts = TableQuery[Posts]

  // Buat tabel users untuk foreign key constraint
  class Users(tag: Tag) extends Table[(UserId, String, String, String)](tag, "users") {
    def id = column[UserId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def email = column[String]("email")
    def passwordHash = column[String]("password_hash")
    
    def * = (id, name, email, passwordHash)
  }

  val users = TableQuery[Users]

  def create(post: Post): Future[Int] = 
    db.run(posts += post)

  def findById(id: PostId): Future[Option[Post]] = 
    db.run(posts.filter(_.id === id).result.headOption)

  def findAll(): Future[Seq[Post]] = 
    db.run(posts.sortBy(_.createdAt.desc).result)

  def update(post: Post): Future[Int] = 
    db.run(posts.filter(_.id === post.id).update(post))

  def delete(id: PostId): Future[Int] = 
    db.run(posts.filter(_.id === id).delete)

  def findByAuthor(authorId: UserId): Future[Seq[Post]] = 
    db.run(posts.filter(_.authorId === authorId).sortBy(_.createdAt.desc).result)
}