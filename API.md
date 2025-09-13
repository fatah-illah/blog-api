# Blog API Documentation

## Authentication
All authenticated endpoints require JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

## Endpoints

### Health Check
```http
GET /health
```
Response:
```json
{
  "status": "success",
  "message": "Server is running",
  "data": null
}
```

### Register
```http
POST /register
Content-Type: application/json

{
  "name": "string",
  "email": "string",
  "password": "string"
}
```
Response:
```json
{
  "status": "success",
  "message": "User registered",
  "data": {
    "id": "uuid",
    "name": "string",
    "email": "string"
  }
}
```

### Login
```http
POST /login
Content-Type: application/json

{
  "email": "string",
  "password": "string"
}
```
Response:
```json
{
  "status": "success",
  "message": "Login successful",
  "data": {
    "token": "string",
    "user": {
      "id": "uuid",
      "name": "string",
      "email": "string"
    }
  }
}
```

### Get All Posts
```http
GET /posts
```
Response:
```json
{
  "status": "success",
  "message": "Posts retrieved",
  "data": [
    {
      "id": "uuid",
      "content": "string",
      "createdAt": "timestamp",
      "updatedAt": "timestamp",
      "authorId": "uuid"
    }
  ]
}
```

### Get Post by ID
```http
GET /posts/{id}
```
Response:
```json
{
  "status": "success",
  "message": "Post retrieved",
  "data": {
    "id": "uuid",
    "content": "string",
    "createdAt": "timestamp",
    "updatedAt": "timestamp",
    "authorId": "uuid"
  }
}
```

### Create Post
```http
POST /posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "string"
}
```
Response:
```json
{
  "status": "success",
  "message": "Post created",
  "data": {
    "id": "uuid",
    "content": "string",
    "createdAt": "timestamp",
    "updatedAt": "timestamp",
    "authorId": "uuid"
  }
}
```

### Update Post
```http
PUT /posts/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "string"
}
```
Response:
```json
{
  "status": "success",
  "message": "Post updated",
  "data": {
    "id": "uuid",
    "content": "string",
    "createdAt": "timestamp",
    "updatedAt": "timestamp",
    "authorId": "uuid"
  }
}
```

### Delete Post
```http
DELETE /posts/{id}
Authorization: Bearer <token>
```
Response:
```json
{
  "status": "success",
  "message": "Post deleted",
  "data": null
}
```

## Error Responses
All endpoints return error responses in this format:
```json
{
  "status": "error",
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- 200: Success
- 201: Created
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error