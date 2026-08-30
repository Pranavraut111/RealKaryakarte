# ── Stage 1: Build the WAR ──
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run in Tomcat ──
FROM tomcat:10.1-jdk21-temurin
# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*
# Copy our WAR as the ROOT app
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
# Create data directory for uploads and passwords
RUN mkdir -p /var/mandal_data/uploads /var/mandal_data/receipts
# Expose port
EXPOSE 8080
CMD ["catalina.sh", "run"]
