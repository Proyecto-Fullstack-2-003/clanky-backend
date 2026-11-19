FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

ARG MODULE

COPY . .

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -pl ${MODULE} -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

ARG MODULE

COPY --from=build /workspace/${MODULE}/target/${MODULE}-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
