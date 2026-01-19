FROM eclipse-temurin:25-jre-alpine

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+UseContainerSupport"

WORKDIR /app

COPY target/rateproducer-0.0.1.jar app.jar

EXPOSE 8181

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
