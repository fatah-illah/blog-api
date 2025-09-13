package com.nolimit.blog.repository

import com.nolimit.blog.domain.{Post, PostId, UserId}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.H2Profile.api._

class PostRepositorySpec extends AnyWordSpec with BeforeAndAfterAll with ScalaFutures with Matchers {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
  
  private var db: Database = _
  private var postRepository: PostRepository = _
  
  private val testUserId = UserId(UUID.randomUUID())
  private val testPostId = PostId(UUID.randomUUID())
  
  override def beforeAll(): Unit = {
    // Setup in-memory database for testing
    db = Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", driver = "org.h2.Driver")
    postRepository = new PostRepository(db)
    
    // Create schema with proper case sensitivity
    val setup = DBIO.seq(
      sqlu"""
        CREATE TABLE "users" (
          "id" UUID PRIMARY KEY,
          "name" VARCHAR(255) NOT NULL,
          "email" VARCHAR(255) UNIQUE NOT NULL,
          "password_hash" VARCHAR(255) NOT NULL
        )
      """,
      sqlu"""
        CREATE TABLE "posts" (
          "id" UUID PRIMARY KEY,
          "content" TEXT NOT NULL,
          "created_at" TIMESTAMP NOT NULL,
          "updated_at" TIMESTAMP NOT NULL,
          "author_id" UUID NOT NULL
        )
      """
    )
    
    db.run(setup).futureValue
  }
  
  override def afterAll(): Unit = {
    db.close()
  }
  
  "PostRepository" should {
    "create and find a post" in {
      val testPost = Post(
        id = testPostId,
        content = "Test content",
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        authorId = testUserId
      )
      
      // Create post
      val createResult = postRepository.create(testPost)
      createResult.futureValue should be(1)
      
      // Find post
      val foundPost = postRepository.findById(testPostId)
      foundPost.futureValue should be(Some(testPost))
    }
    
    "return None when post not found" in {
      val nonExistentId = PostId(UUID.randomUUID())
      val result = postRepository.findById(nonExistentId)
      result.futureValue should be(None)
    }
  }
}