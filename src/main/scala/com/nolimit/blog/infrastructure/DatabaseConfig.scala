package com.nolimit.blog.infrastructure

import slick.jdbc.PostgresProfile.api._
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext

class DatabaseConfig(implicit ec: ExecutionContext) {
  private val config = ConfigFactory.load()
  
  private val databaseUrl = config.getString("database.properties.url")
  private val databaseUser = config.getString("database.properties.user")
  private val databasePassword = config.getString("database.properties.password")
  
  val db = Database.forURL(
    url = databaseUrl,
    user = databaseUser,
    password = databasePassword,
    driver = "org.postgresql.Driver"
  )
}