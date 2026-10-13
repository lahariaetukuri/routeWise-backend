FROM eclipse-temurin:19-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw -DskipTests package

EXPOSE 8081

CMD ["sh", "-c", "java -Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -Xss512k -jar target/routeWise-0.0.1-SNAPSHOT.jar --server.port=${PORT:-8081}"]
