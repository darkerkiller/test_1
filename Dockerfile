# Multi-stage build for Java application with GUI support
FROM openjdk:11-jdk-slim as builder

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
COPY src/main/resources/config.yml src/main/resources/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/
COPY extract_pdf.py .

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:11-jre-slim

# Install required packages for GUI support and Python
RUN apt-get update && \
    apt-get install -y \
        python3 \
        python3-pip \
        libxext6 \
        libxrender1 \
        libxtst6 \
        libxi6 \
        libfontconfig1 \
        fonts-dejavu-core \
        && rm -rf /var/lib/apt/lists/*

# Install Python dependencies for PDF conversion
RUN pip3 install reportlab python-docx

# Create application directory
WORKDIR /app

# Copy built JAR and resources from builder stage
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/extract_pdf.py .
COPY --from=builder /app/src/main/resources/config.yml config.yml

# Create documents directory for file operations
RUN mkdir -p /app/documents

# Set environment variables
ENV DISPLAY=:0
ENV JAVA_OPTS="-Djava.awt.headless=false"

# Create non-root user for security
RUN useradd -m -u 1000 texteditor && \
    chown -R texteditor:texteditor /app
USER texteditor

# Expose volume for documents
VOLUME ["/app/documents"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD pgrep -f "java.*app.jar" || exit 1

# Default command
CMD ["java", "-jar", "app.jar"]

# Alternative command for headless mode
# CMD ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]