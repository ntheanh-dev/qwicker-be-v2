FROM openjdk:22-jdk
ARG JAR_FILE=target/*.jar
COPY ./target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]


## First stage: Build
#FROM openjdk:22-jdk-slim as build
#RUN apt-get update \
#    && apt-get install -y maven \
#    && rm -rf /var/lib/apt/lists/*
#WORKDIR /app
#COPY . .
## Build the application using Maven, skipping tests
#RUN mvn clean package -DskipTests
#
## Second stage: Production
#FROM openjdk:22-jdk-slim as production
#WORKDIR /app
## Copy the JAR file from the Maven build
#COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
## Run the application
#ENTRYPOINT ["java","-jar","app.jar"]

