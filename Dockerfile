# Multi-stage Dockerfile para Spreadsheet Generator

# Estágio 1: Build
FROM openjdk:17-jdk-slim AS builder

# Instalar Maven
RUN apt-get update && apt-get install -y maven

# Configurar diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Baixar dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Fazer build da aplicação
RUN ./mvnw clean package -DskipTests -B

# Estágio 2: Runtime
FROM openjdk:17-jre-slim AS runtime

# Criar usuário não-root para segurança
RUN groupadd -r springboot && useradd -r -g springboot springboot

# Instalar utilitários necessários
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Configurar diretório de trabalho
WORKDIR /app

# Criar diretórios necessários
RUN mkdir -p /app/temp /app/logs && \
    chown -R springboot:springboot /app

# Copiar JAR da aplicação do estágio de build
COPY --from=builder /app/target/*.jar app.jar

# Alterar ownership do JAR
RUN chown springboot:springboot app.jar

# Configurar usuário
USER springboot

# Expor porta da aplicação
EXPOSE 8080

# Configurar healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Configurar JVM para container
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=70.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels para metadados
LABEL maintainer="dev@filestreamer.com"
LABEL version="1.0.0"
LABEL description="Spreadsheet Generator Application" 