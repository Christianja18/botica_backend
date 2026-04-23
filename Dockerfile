#Etapa 1: Construccion del JAR
#Dockerfile esta dividido en 2 capas
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

RUN ls -la /app/target

#Etapa 2: Costruccion de la imagen final
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

ENV DB_URL=jdbc:mysql://db:3306/botica_db
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV SERVER_PORT=8080
ENV JPA_SHOW_SQL=false

COPY --from=build /app/target/botica-0.0.1-SNAPSHOT.jar /app/botica.jar

EXPOSE 8080

ENTRYPOINT ["java","-Xms256m","-Xmx512m","-XX:+UseG1GC","-XX:MaxGCPauseMillis=200","-XX:InitiatingHeapOccupancyPercent=35","-XX:+ParallelRefProcEnabled","-jar","/app/botica.jar"]
