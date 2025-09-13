package com.nolimit.blog

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import com.nolimit.blog.service.{JwtService, UserService, PostService}
import com.nolimit.blog.domain._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import scala.concurrent.Future
import java.util.UUID
import java.time.Instant

class RoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {
  
  val testUserId = UserId(UUID.randomUUID())
  val testUser = User(testUserId, "Test User", "test@example.com", "password-hash")
  val testToken = "test-token"
  
  class MockUserService extends UserService(null) {
    override def register(user: User): Future[Either[String, User]] = {
      Future.successful(Right(testUser))
    }
    
    override def login(email: String, password: String): Future[Either[String, User]] = {
      if (email == "test@example.com" && password == "password") {
        Future.successful(Right(testUser))
      } else {
        Future.successful(Left("Invalid credentials"))
      }
    }
  }
  
  class MockPostService extends PostService(null) {
    val testPost = Post(PostId(UUID.randomUUID()), "Test content", Instant.now(), Instant.now(), testUserId)
    
    override def getAllPosts: Future[Seq[Post]] = {
      Future.successful(Seq(testPost))
    }
    
    override def getPost(id: PostId): Future[Option[Post]] = {
      Future.successful(Some(testPost))
    }
    
    override def createPost(post: Post): Future[Either[String, Post]] = {
      Future.successful(Right(post))
    }
  }
  
  class MockJwtService extends JwtService("test-secret") {
    override def createToken(userId: String): String = testToken
    override def validateToken(token: String): Either[String, String] = {
      if (token == testToken) Right(testUserId.value.toString)
      else Left("Invalid token")
    }
  }
  
  val routes = new Routes(new MockJwtService, new MockUserService, new MockPostService).routes
  
  "Routes" should {
    "return health check" in {
      Get("/health") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        val response = responseAs[StandardResponse]
        response.status shouldBe "success"
        response.message shouldBe "Server is running"
      }
    }
    
    "register new user" in {
      val request = RegisterRequest("Test User", "test@example.com", "password")
      Post("/register", request) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        val response = responseAs[StandardResponse]
        response.status shouldBe "success"
        response.message shouldBe "User registered"
      }
    }
    
    "login user" in {
      val request = LoginRequest("test@example.com", "password")
      Post("/login", request) ~> routes ~> check {
        status shouldBe StatusCodes.OK
        val response = responseAs[StandardResponse]
        response.status shouldBe "success"
        response.message shouldBe "Login successful"
      }
    }
    
    "get all posts" in {
      Get("/posts") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        val response = responseAs[StandardResponse]
        response.status shouldBe "success"
        response.message shouldBe "Posts retrieved"
      }
    }
    
    "create post with valid token" in {
      val request = CreatePostRequest("Test content")
      Post("/posts", request)
        .addHeader(Authorization(OAuth2BearerToken(testToken))) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        val response = responseAs[StandardResponse]
        response.status shouldBe "success"
        response.message shouldBe "Post created"
      }
    }
  }
}