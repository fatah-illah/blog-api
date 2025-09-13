package com.nolimit.blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import com.nolimit.blog.service.{JwtService, UserService, PostService}
import com.nolimit.blog.domain._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import com.nolimit.blog.domain.CirceFormats._
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}
import java.util.UUID
import java.time.Instant

class Routes(jwtService: JwtService, userService: UserService, postService: PostService)(implicit ec: ExecutionContext) {

  private def authenticateToken(token: String): Option[UserId] = {
    println(s"Authenticating token: ${token.take(50)}...")
    val cleanToken = token.replace("Bearer ", "")
    println(s"Clean token: ${cleanToken.take(50)}...")
    jwtService.validateToken(cleanToken).toOption.map(userIdStr => UserId(UUID.fromString(userIdStr)))
  }

  val routes: Route =
    path("health") {
      get {
        complete(StandardResponse("success", "Server is running", None))
      }
    } ~
    path("register") {
      post {
        entity(as[RegisterRequest]) { request =>
          onComplete(userService.register(User(UserId(UUID.randomUUID()), request.name, request.email, request.password))) {
            case Success(Right(user)) =>
              val userResponse = UserResponse(user.id.value, user.name, user.email)
              complete(StatusCodes.Created, StandardResponse("success", "User registered", Some(userResponse)))
            case Success(Left(errorMessage)) =>
              complete(StatusCodes.BadRequest, StandardResponse("error", errorMessage, None))
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
          }
        }
      }
    } ~
    path("login") {
      post {
        entity(as[LoginRequest]) { request =>
          onComplete(userService.login(request.email, request.password)) {
            case Success(Right(user)) =>
              val token = jwtService.createToken(user.id.value.toString)
              val userResponse = UserResponse(user.id.value, user.name, user.email)
              complete(StandardResponse("success", "Login successful", Some(AuthResponse(token, userResponse))))
            case Success(Left(errorMessage)) =>
              complete(StatusCodes.Unauthorized, StandardResponse("error", errorMessage, None))
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
          }
        }
      }
    } ~
    path("posts") {
      get {
        onComplete(postService.getAllPosts) {
          case Success(posts) =>
            val postResponses = posts.map { post =>
              PostResponse(post.id.value, post.content, post.createdAt, post.updatedAt, post.authorId.value)
            }
            complete(StandardResponse("success", "Posts retrieved", Some(postResponses)))
          case Failure(ex) =>
            complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
        }
      } ~
      post {
        headerValueByName("Authorization") { token =>
          val userIdOpt = authenticateToken(token)
          userIdOpt match {
            case Some(userId) =>
              entity(as[CreatePostRequest]) { request =>
                val postId = PostId(UUID.randomUUID())
                val now = Instant.now()
                val post = Post(postId, request.content, now, now, userId)
                onComplete(postService.createPost(post)) {
                  case Success(Right(createdPost)) =>
                    val postResponse = PostResponse(createdPost.id.value, createdPost.content, createdPost.createdAt, createdPost.updatedAt, createdPost.authorId.value)
                    complete(StatusCodes.Created, StandardResponse("success", "Post created", Some(postResponse)))
                  case Success(Left(errorMessage)) =>
                    complete(StatusCodes.BadRequest, StandardResponse("error", errorMessage, None))
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
                }
              }
            case None =>
              complete(StatusCodes.Unauthorized, StandardResponse("error", "Invalid token", None))
          }
        }
      }
    } ~
    path("posts" / JavaUUID) { id =>
      get {
        onComplete(postService.getPost(PostId(id))) {
          case Success(Some(post)) =>
            val postResponse = PostResponse(post.id.value, post.content, post.createdAt, post.updatedAt, post.authorId.value)
            complete(StandardResponse("success", "Post retrieved", Some(postResponse)))
          case Success(None) =>
            complete(StatusCodes.NotFound, StandardResponse("error", "Post not found", None))
          case Failure(ex) =>
            complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
        }
      } ~
      put {
        headerValueByName("Authorization") { token =>
          val userIdOpt = authenticateToken(token)
          userIdOpt match {
            case Some(userId) =>
              entity(as[UpdatePostRequest]) { request =>
                onComplete(postService.updatePost(PostId(id), request.content, userId)) {
                  case Success(Right(updatedPost)) =>
                    val postResponse = PostResponse(updatedPost.id.value, updatedPost.content, updatedPost.createdAt, updatedPost.updatedAt, updatedPost.authorId.value)
                    complete(StandardResponse("success", "Post updated", Some(postResponse)))
                  case Success(Left(errorMessage)) =>
                    complete(StatusCodes.BadRequest, StandardResponse("error", errorMessage, None))
                  case Failure(ex) =>
                    complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
                }
              }
            case None =>
              complete(StatusCodes.Unauthorized, StandardResponse("error", "Invalid token", None))
          }
        }
      } ~
      delete {
        headerValueByName("Authorization") { token =>
          val userIdOpt = authenticateToken(token)
          userIdOpt match {
            case Some(userId) =>
              onComplete(postService.deletePost(PostId(id), userId)) {
                case Success(Right(_)) =>
                  complete(StandardResponse("success", "Post deleted", None))
                case Success(Left(errorMessage)) =>
                  complete(StatusCodes.BadRequest, StandardResponse("error", errorMessage, None))
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError, StandardResponse("error", ex.getMessage, None))
              }
            case None =>
              complete(StatusCodes.Unauthorized, StandardResponse("error", "Invalid token", None))
          }
        }
      }
    }
}