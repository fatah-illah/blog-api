package com.nolimit.blog.service

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import java.time.Instant
import scala.util.{Try, Success, Failure}

class JwtService(secretKey: String) {
  private val algorithm = JwtAlgorithm.HS256

  def createToken(userId: String): String = {
    val now = Instant.now.getEpochSecond
    val claim = JwtClaim(
      expiration = Some(now + 86400),  // 24 hours from now
      issuedAt = Some(now),
      subject = Some(userId)
    )
    println(s"Creating token with claim: ${claim}")
    println(s"Subject in claim: ${claim.subject}")
    Jwt.encode(claim, secretKey, algorithm)
  }

  def validateToken(token: String): Try[String] = {
    Jwt.decode(token, secretKey, Seq(algorithm)) flatMap { claim =>
      // Try to get subject from the claim object first
      claim.subject match {
        case Some(userId) => Success(userId)
        case None => 
          // Fallback: parse the subject from the raw JSON content
          import io.circe.parser._
          parse(claim.content) match {
            case Right(json) =>
              json.hcursor.get[String]("sub") match {
                case Right(userId) => Success(userId)
                case Left(_) => Failure(new Exception("No subject found in token"))
              }
            case Left(_) => Failure(new Exception("Invalid token format"))
          }
      }
    }
  }
}