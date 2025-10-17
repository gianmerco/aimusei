# ========== BUILD ==========
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1) Copia TUTTO
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY aiservice/pom.xml aiservice/pom.xml
COPY aimusei/pom.xml aimusei/pom.xml
COPY common/src common/src
COPY aiservice/src aiservice/src
COPY aimusei/src aimusei/src

# 2) Build dell'app (scarica dipendenze e compila tutto insieme)
RUN mvn -B -DskipTests -pl aimusei -am clean package
# ========== RUNTIME ==========
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copia il JAR "repackaged" dello Spring Boot plugin (non *.original)
COPY --from=build /workspace/aimusei/target/*[!-original].jar /app/app.jar
EXPOSE 8090
CMD ["java","-jar","/app/app.jar"]