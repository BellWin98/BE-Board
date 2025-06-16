FROM openjdk:17-jdk

ARG JAR_FILE=build/libs/*.jar

ENV JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_ENCRYPTOR_PASSWORD}

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/app.jar"]