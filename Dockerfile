FROM eclipse-temurin:17-jdk-focal

# Install sbt
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fL "https://github.com/sbt/sbt/releases/download/v1.9.6/sbt-1.9.6.tgz" -o sbt.tgz && \
    tar -xzf sbt.tgz && \
    mv sbt /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    rm sbt.tgz && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy build files first for better caching
COPY build.sbt ./
COPY project ./project/

# Run sbt update to cache dependencies
RUN sbt update

# Copy source code
COPY src ./src/

# Build the application
RUN sbt clean compile

# Copy .jvmopts file
COPY .jvmopts /root/.jvmopts

# Copy and set permissions for entrypoint script
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

# Expose port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["/app/docker-entrypoint.sh"]

# Set Java options and run the application
ENV JAVA_OPTS="-Xmx512m -Xms256m"
CMD sbt ${JAVA_OPTS} run