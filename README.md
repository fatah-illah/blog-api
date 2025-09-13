# Blog API

A RESTful blog API built with Scala, Akka HTTP, and PostgreSQL. This API provides endpoints for user authentication and blog post management.

## Features

- **User Authentication**: Register and login with JWT token-based authentication
- **Blog Post Management**: Create, read, update, and delete blog posts
- **Database Integration**: PostgreSQL with Slick ORM and Flyway migrations
- **Docker Support**: Containerized application with Docker Compose
- **RESTful API**: Clean REST endpoints with JSON responses

## Tech Stack

- **Scala 2.13.10**
- **Akka HTTP** - HTTP server and routing
- **Slick** - Database ORM
- **PostgreSQL** - Database
- **Flyway** - Database migrations
- **JWT** - Authentication
- **Circe** - JSON serialization
- **SBT** - Build tool

## Project Structure

```
src/
├── main/
│   ├── resources/
│   │   ├── application.conf          # Configuration file
│   │   ├── application.conf.example  # Example configuration
│   │   ├── db/migration/             # Database migrations
│   │   └── logback.xml               # Logging configuration
│   └── scala/com/nolimit/blog/
│       ├── Boot.scala               # Application entry point
│       ├── Routes.scala             # HTTP routes
│       ├── domain/                  # Domain models and DTOs
│       ├── service/                 # Business logic services
│       ├── repository/              # Data access layer
│       └── infrastructure/          # Database and external services
└── test/                            # Test files
```

## Prerequisites

- Java 8 or higher
- SBT (Scala Build Tool)
- PostgreSQL
- Docker (optional)

## Installation

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd blog-api
   ```

2. **Setup PostgreSQL database**
   ```bash
   # Create database
   createdb blogdb
   ```

3. **Configure application**
   ```bash
   # Copy example configuration
   cp src/main/resources/application.conf.example src/main/resources/application.conf
   
   # Edit configuration with your database settings
   nano src/main/resources/application.conf
   ```

4. **Run database migrations**
   ```bash
   sbt run
   ```

5. **Start the application**
   ```bash
   sbt run
   ```

The API will be available at `http://localhost:8080`

### Docker Development

1. **Start with Docker Compose**
   ```bash
   docker-compose up -d
   ```

2. **Check logs**
   ```bash
   docker-compose logs -f
   ```

## Configuration

Edit `src/main/resources/application.conf`:

```hocon
akka {
  http {
    server {
      parsing {
        max-content-length = 10m
      }
    }
  }
}

jwt {
  secret = "your-256-bit-secret"
  expiration = "24 hours"
}

database {
  properties {
    url = "jdbc:postgresql://localhost:5432/blogdb"
    user = "postgres"
    password = "your-password"
  }
  numThreads = 10
}
```

## API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication
All protected endpoints require JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

### Endpoints

#### Health Check
```http
GET /health
```

#### User Registration
```http
POST /register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### User Login
```http
POST /login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Get All Posts
```http
GET /posts
```

#### Get Post by ID
```http
GET /posts/{id}
```

#### Create Post
```http
POST /posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "This is my blog post content"
}
```

#### Update Post
```http
PUT /posts/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Updated blog post content"
}
```

#### Delete Post
```http
DELETE /posts/{id}
Authorization: Bearer <token>
```

For detailed API documentation, see [API.md](API.md).

## Response Format

All API responses follow this format:

```json
{
  "status": "success|error",
  "message": "Description message",
  "data": <response_data_or_null>
}
```

## Error Handling

The API returns appropriate HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

## Development

### Running Tests
```bash
sbt test
```

### Building
```bash
sbt compile
```

### Packaging
```bash
sbt package
```

### Running in Development Mode
```bash
sbt ~reStart
```

## Database Schema

The application uses the following main tables:

- **users**: User accounts with authentication
- **posts**: Blog posts with content and metadata

Database migrations are handled by Flyway and located in `src/main/resources/db/migration/`.

## Docker

### Build Docker Image
```bash
docker build -t blog-api .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please open an issue in the repository.
