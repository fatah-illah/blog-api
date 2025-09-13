package com.nolimit.blog.repository

import com.nolimit.blog.domain.{User, UserId}
import slick.jdbc.PostgresProfile.api._
import java.util.UUID
import scala.concurrent.Future

class UserRepository(db: Database) {
  // Custom column type untuk UUID
  implicit val userIdColumnType = MappedColumnType.base[UserId, UUID](
    userId => userId.value,
    uuid => UserId(uuid)
  )

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[UserId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def email = column[String]("email", O.Unique)
    def passwordHash = column[String]("password_hash")
    
    def * = (id, name, email, passwordHash) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]
  
  def create(user: User): Future[Int] = 
    db.run(users += user)
  
  def findByEmail(email: String): Future[Option[User]] = 
    db.run(users.filter(_.email === email).result.headOption)
}