package com.nolimit.blog.infrastructure

import org.flywaydb.core.Flyway
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.Config

class FlywayService(config: Config) extends LazyLogging {
  private val dbUrl = config.getString("database.properties.url")
  private val dbUser = config.getString("database.properties.user")
  private val dbPassword = config.getString("database.properties.password")

  private val flyway = Flyway.configure()
    .dataSource(dbUrl, dbUser, dbPassword)
    .locations("db/migration")
    .load()

  def migrateDatabases(): Unit = {
    logger.info("Starting database migration...")
    try {
      val migrations = flyway.migrate()
      logger.info(s"Successfully applied ${migrations.migrationsExecuted} migrations")
    } catch {
      case e: Exception =>
        logger.error("Failed to apply migrations", e)
        throw e
    }
  }
}