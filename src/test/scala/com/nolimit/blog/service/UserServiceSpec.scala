package com.nolimit.blog.service

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures
import com.nolimit.blog.domain.{User, UserId}
import com.nolimit.blog.repository.UserRepository
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

class UserServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {
  
  class MockUserRepository extends UserRepository(null) {
    override def findByEmail(email: String): Future[Option[User]] = {
      if (email == "existing@example.com") {
        Future.successful(Some(User(
          UserId(UUID.randomUUID()),
          "Existing User",
          email,
          "$2a$10$8K1p/a0dL1LXMIgoEDFrwO2USOIwP0VzuJWsP4zqfWfYGIBJsSKIS" // "password" hashed
        )))
      } else {
        Future.successful(None)
      }
    }

    override def create(user: User): Future[Int] = {
      Future.successful(1)
    }
  }

  "UserService" should {
    val userService = new UserService(new MockUserRepository)

    "register a new user successfully" in {
      val newUser = User(
        UserId(UUID.randomUUID()),
        "Test User",
        "test@example.com",
        "password"
      )

      val result = userService.register(newUser).futureValue
      result.isRight shouldBe true
      result.right.get.email shouldBe "test@example.com"
      result.right.get.passwordHash shouldBe ""  // Password hash should not be returned
    }

    "reject registration with invalid email" in {
      val newUser = User(
        UserId(UUID.randomUUID()),
        "Test User",
        "invalid-email",
        "password"
      )

      val result = userService.register(newUser).futureValue
      result.isLeft shouldBe true
      result.left.get shouldBe "Invalid email format"
    }

    "reject registration with existing email" in {
      val newUser = User(
        UserId(UUID.randomUUID()),
        "Test User",
        "existing@example.com",
        "password"
      )

      val result = userService.register(newUser).futureValue
      result.isLeft shouldBe true
      result.left.get shouldBe "Email already exists"
    }

    "login successfully with correct credentials" in {
      val result = userService.login("existing@example.com", "password").futureValue
      result.isRight shouldBe true
      result.right.get.email shouldBe "existing@example.com"
      result.right.get.passwordHash shouldBe ""  // Password hash should not be returned
    }

    "reject login with incorrect password" in {
      val result = userService.login("existing@example.com", "wrong-password").futureValue
      result.isLeft shouldBe true
      result.left.get shouldBe "Invalid password"
    }

    "reject login with non-existent email" in {
      val result = userService.login("nonexistent@example.com", "password").futureValue
      result.isLeft shouldBe true
      result.left.get shouldBe "User not found"
    }
  }
}