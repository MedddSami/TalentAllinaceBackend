# use an official maven image to build the spring boot app
# FROM maven:3.9.6-openjdk-21 AS build
FROM maven:3.9-eclipse-temurin-21 AS build

# set the working directory
WORKDIR /app

# copy the pom.xml and install dependencies
COPY pom.xml .

# RUN mvn dependency:go-offline
RUN mvn dependency:go-offline -B

# copy the source code and build the application
COPY src ./src
# RUN mvn clean package -DskipTests
RUN mvn clean package -DskipTests -B

# use an official openjdk image to run the application
FROM eclipse-temurin:21-jdk-jammy AS runtime

# set the working directory
WORKDIR /app

# copy the built JAR file from the build stage
COPY --from=build /app/target/JobRecuruitmentPlatform-0.0.1-SNAPSHOT.jar .

# expose port 8080
EXPOSE 8080

# specify the command to run the application
ENTRYPOINT ["java", "-jar", "/app/JobRecuruitmentPlatform-0.0.1-SNAPSHOT.jar"]