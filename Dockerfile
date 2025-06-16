FROM openjdk:17-jdk

ARG JAR_FILE=build/libs/*.jar

ENV JASYPT_KEY=${JASYPT_KEY}

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/app.jar"]