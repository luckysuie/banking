FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/digital-banking-platform-*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=azure
ENTRYPOINT ["java", "-jar", "app.jar"]
