package com.nolimit.blog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.nolimit.blog.service.{JwtService, UserService, PostService}
import com.nolimit.blog.repository.{UserRepository, PostRepository}
import com.nolimit.blog.infrastructure.DatabaseConfig
import scala.util.{Success, Failure}
import scala.concurrent.{ExecutionContext, Future, Promise, Await, blocking}
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging
import com.nolimit.blog.infrastructure.FlywayService
import akka.Done
import akka.actor.CoordinatedShutdown

object Boot extends LazyLogging {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("blog-api")
    implicit val ec: ExecutionContext = system.dispatcher

    val config = ConfigFactory.load()
    val jwtService = new JwtService(config.getString("jwt.secret"))
    
    // Run database migrations
    logger.info("Initializing database migrations...")
    val flywayService = new FlywayService(config)
    flywayService.migrateDatabases()
    
    // Setup database
    val dbConfig = new DatabaseConfig
    val userRepository = new UserRepository(dbConfig.db)
    val postRepository = new PostRepository(dbConfig.db)
    
    // Setup services
    val userService = new UserService(userRepository)
    val postService = new PostService(postRepository)
    
    // Initialize routes
    val routes = new Routes(jwtService, userService, postService).routes

    // FIXED: Bind to "0.0.0.0" instead of "localhost"
    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes)

    bindingFuture.onComplete {
      case Success(binding) =>
        logger.info(s"\nServer online at http://${binding.localAddress}")
        logger.info("\nPress ENTER to stop the server...")
      case Failure(exception) =>
        logger.error("Failed to bind to port 8080!", exception)
        system.terminate()
    }

    // Keep the server running
    // In Docker, StdIn.readLine() returns immediately, so we use a different approach
    val isDocker = sys.env.get("DOCKER_ENV").isDefined || System.console() == null
    
    if (isDocker) {
      // In Docker: Keep running indefinitely
      logger.info("Running in Docker mode - server will run indefinitely")
      Await.result(system.whenTerminated, Duration.Inf)
    } else {
      // Local development: Wait for user input
      logger.info("Press ENTER to stop the server...")
      scala.io.StdIn.readLine()
      
      // Graceful shutdown
      bindingFuture.foreach { binding =>
        binding.terminate(10.seconds)
      }
      system.terminate()
    }
  }
}