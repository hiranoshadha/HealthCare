FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/appointment-service-0.0.1-SNAPSHOT.jar appointment-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "appointment-service.jar"]