FROM maven:3.9.0-eclipse-temurin-19 AS build

WORKDIR /app

COPY . .
COPY maven-settings.xml /root/.m2/settings.xml

RUN mvn -B -DskipTests package

FROM eclipse-temurin:19-jre

WORKDIR /app

COPY --from=build /app/target/routeWise-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["sh", "-c", "java -Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -Xss512k -jar app.jar --server.port=${PORT:-8081}"]
