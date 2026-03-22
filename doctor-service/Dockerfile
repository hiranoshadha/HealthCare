FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/doctor-service-0.0.1-SNAPSHOT.jar doctor-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "doctor-service.jar"]