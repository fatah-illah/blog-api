package com.nolimit.blog.domain

import java.util.UUID
import java.time.Instant

case class UserId(value: UUID) extends AnyVal
case class PostId(value: UUID) extends AnyVal

case class User(
  id: UserId,
  name: String,
  email: String,
  passwordHash: String
)

case class Post(
  id: PostId,
  content: String,
  createdAt: Instant,
  updatedAt: Instant,
  authorId: UserId
)