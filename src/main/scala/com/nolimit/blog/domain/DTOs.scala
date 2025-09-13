package com.nolimit.blog.domain

import java.util.UUID
import java.time.Instant

case class RegisterRequest(name: String, email: String, password: String)
case class LoginRequest(email: String, password: String)
case class CreatePostRequest(content: String)
case class UpdatePostRequest(content: String)

case class UserResponse(id: UUID, name: String, email: String)
case class PostResponse(id: UUID, content: String, createdAt: Instant, updatedAt: Instant, authorId: UUID)

case class AuthResponse(token: String, user: UserResponse)

case class StandardResponse(status: String, message: String, data: Option[Any] = None)