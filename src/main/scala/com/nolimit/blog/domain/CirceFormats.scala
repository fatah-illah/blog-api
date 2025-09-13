package com.nolimit.blog.domain

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._
import java.util.UUID
import java.time.Instant

object CirceFormats {
  implicit val uuidEncoder: Encoder[UUID] = Encoder.encodeString.contramap(_.toString)
  implicit val uuidDecoder: Decoder[UUID] = Decoder.decodeString.map(UUID.fromString)

  implicit val instantEncoder: Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.map(Instant.parse)

  implicit val registerRequestDecoder: Decoder[RegisterRequest] = deriveDecoder[RegisterRequest]
  implicit val registerRequestEncoder: Encoder[RegisterRequest] = deriveEncoder[RegisterRequest]

  implicit val loginRequestDecoder: Decoder[LoginRequest] = deriveDecoder[LoginRequest]
  implicit val loginRequestEncoder: Encoder[LoginRequest] = deriveEncoder[LoginRequest]

  implicit val createPostRequestDecoder: Decoder[CreatePostRequest] = deriveDecoder[CreatePostRequest]
  implicit val createPostRequestEncoder: Encoder[CreatePostRequest] = deriveEncoder[CreatePostRequest]

  implicit val updatePostRequestDecoder: Decoder[UpdatePostRequest] = deriveDecoder[UpdatePostRequest]
  implicit val updatePostRequestEncoder: Encoder[UpdatePostRequest] = deriveEncoder[UpdatePostRequest]

  implicit val userResponseEncoder: Encoder[UserResponse] = deriveEncoder[UserResponse]
  implicit val userResponseDecoder: Decoder[UserResponse] = deriveDecoder[UserResponse]

  implicit val postResponseEncoder: Encoder[PostResponse] = deriveEncoder[PostResponse]
  implicit val postResponseDecoder: Decoder[PostResponse] = deriveDecoder[PostResponse]

  implicit val authResponseEncoder: Encoder[AuthResponse] = deriveEncoder[AuthResponse]
  implicit val authResponseDecoder: Decoder[AuthResponse] = deriveDecoder[AuthResponse]

  implicit val standardResponseEncoder: Encoder[StandardResponse] = new Encoder[StandardResponse] {
    def apply(response: StandardResponse): io.circe.Json = {
      import io.circe.Json
      Json.obj(
        "status" -> Json.fromString(response.status),
        "message" -> Json.fromString(response.message),
        "data" -> response.data.fold(Json.Null)(value => Json.fromString(value.toString))
      )
    }
  }

  implicit val standardResponseDecoder: Decoder[StandardResponse] = new Decoder[StandardResponse] {
    def apply(c: io.circe.HCursor): io.circe.Decoder.Result[StandardResponse] = {
      for {
        status <- c.downField("status").as[String]
        message <- c.downField("message").as[String]
        data <- c.downField("data").as[Option[String]]
      } yield StandardResponse(status, message, data.map(identity))
    }
  }
}