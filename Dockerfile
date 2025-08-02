FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
EXPOSE 8080 5005
ENTRYPOINT ["java", "-jar", "app.jar"]