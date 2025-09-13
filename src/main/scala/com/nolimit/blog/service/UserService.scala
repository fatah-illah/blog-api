package com.nolimit.blog.service

import com.nolimit.blog.domain.{User, UserId}
import com.nolimit.blog.repository.UserRepository
import scala.concurrent.{Future, ExecutionContext}
import java.util.UUID
import scala.util.{Success, Failure, Try}
import com.github.t3hnar.bcrypt._
import java.util.regex.Pattern

class UserService(userRepository: UserRepository)(implicit ec: ExecutionContext) {
  private val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"
  private val emailPattern = Pattern.compile(emailRegex)
  
  private def validateEmail(email: String): Boolean = {
    emailPattern.matcher(email).matches()
  }
  
  private def hashPassword(password: String): String = {
    password.boundedBcrypt
  }
  
  private def verifyPassword(password: String, hash: String): Boolean = {
    password.isBcryptedBounded(hash)
  }
  def register(user: User): Future[Either[String, User]] = {
    if (!validateEmail(user.email)) {
      return Future.successful(Left("Invalid email format"))
    }
    
    userRepository.findByEmail(user.email).flatMap {
      case Some(_) => Future.successful(Left("Email already exists"))
      case None => 
        val hashedUser = user.copy(passwordHash = hashPassword(user.passwordHash))
        userRepository.create(hashedUser).map { _ =>
          Right(hashedUser.copy(passwordHash = "")) // Don't send password hash back
        }
    }
  }

  def login(email: String, password: String): Future[Either[String, User]] = {
    userRepository.findByEmail(email).map {
      case Some(user) if verifyPassword(password, user.passwordHash) => 
        Right(user.copy(passwordHash = "")) // Don't send password hash back
      case Some(_) => Left("Invalid password")
      case None => Left("User not found")
    }
  }
}