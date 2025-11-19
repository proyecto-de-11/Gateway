
# Primera fase: Construcción
# Usa una imagen de Maven que incluye OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos de Maven (pom.xml) y el código fuente (src)
COPY pom.xml .
COPY src ./src

# Compila el proyecto. Esto genera el JAR en /app/target/
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# --- Segunda fase: Ejecución ---
# Usamos 'eclipse-temurin' con el JRE 21 (Alpine) para una imagen ligera y estable.
FROM eclipse-temurin:21-jre-alpine

# Establece el directorio de trabajo
WORKDIR /app

# Copia el JAR generado y lo renombra a 'app.jar'.
COPY --from=build /app/target/*.jar /app/app.jar

# Expone el puerto de la aplicación.
EXPOSE 8080

# Comando para ejecutar la aplicación
# NOTA: Usamos el puerto 8081.
CMD ["java", "-jar", "/app/app.jar"]
