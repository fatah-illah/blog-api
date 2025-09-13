package com.nolimit.blog.service

import com.nolimit.blog.domain.{Post, PostId, UserId}
import com.nolimit.blog.repository.PostRepository
import scala.concurrent.{Future, ExecutionContext}
import java.time.Instant

class PostService(postRepository: PostRepository)(implicit ec: ExecutionContext) {
  def createPost(post: Post): Future[Either[String, Post]] = {
    postRepository.create(post).map { _ =>
      Right(post)
    }
  }

  def getPost(id: PostId): Future[Option[Post]] = {
    postRepository.findById(id)
  }

  def getAllPosts: Future[Seq[Post]] = {
    postRepository.findAll()
  }

  def updatePost(id: PostId, content: String, userId: UserId): Future[Either[String, Post]] = {
    postRepository.findById(id).flatMap {
      case Some(post) if post.authorId == userId =>
        val updatedPost = post.copy(content = content, updatedAt = Instant.now())
        postRepository.update(updatedPost).map { _ =>
          Right(updatedPost)
        }
      case Some(_) => Future.successful(Left("Not authorized to update this post"))
      case None => Future.successful(Left("Post not found"))
    }
  }

  def deletePost(id: PostId, userId: UserId): Future[Either[String, Unit]] = {
    postRepository.findById(id).flatMap {
      case Some(post) if post.authorId == userId =>
        postRepository.delete(id).map { _ =>
          Right(())
        }
      case Some(_) => Future.successful(Left("Not authorized to delete this post"))
      case None => Future.successful(Left("Post not found"))
    }
  }

  def getPostsByUser(userId: UserId): Future[Seq[Post]] = {
    postRepository.findByAuthor(userId)
  }
}