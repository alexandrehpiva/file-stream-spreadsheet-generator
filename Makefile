# Makefile para Spreadsheet Generator

.PHONY: help build run test clean docker-up docker-down migrate install

# Configurações
JAVA_VERSION := 24
MAVEN_OPTS := -Xmx2g
DOCKER_COMPOSE_FILE := docker-compose.yml

# Ajuda
help: ## Mostrar esta ajuda
	@echo "Comandos disponíveis:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Instalação e configuração
install: ## Instalar dependências e configurar projeto
	@echo "📦 Instalando dependências..."
	./mvnw clean install -DskipTests
	@echo "✅ Dependências instaladas com sucesso!"

# Banco de dados
docker-up: ## Subir o banco PostgreSQL com Docker Compose
	@echo "🐳 Iniciando PostgreSQL..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d postgres
	@echo "⏳ Aguardando PostgreSQL inicializar..."
	@sleep 10
	@echo "✅ PostgreSQL iniciado!"

docker-down: ## Parar o banco PostgreSQL
	@echo "🛑 Parando PostgreSQL..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) down
	@echo "✅ PostgreSQL parado!"

docker-logs: ## Ver logs do PostgreSQL
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f postgres

# Desenvolvimento
dev: docker-up ## Iniciar ambiente de desenvolvimento completo com hot-reload
	@echo "🚀 Iniciando aplicação em modo desenvolvimento com hot-reload..."
	@echo "💡 DevTools habilitado - mudanças nos arquivos .java serão aplicadas automaticamente"
	@echo "🔄 LiveReload disponível na porta 35729"
	./mvnw spring-boot:run -Dspring-boot.run.profiles=development

run: ## Executar aplicação (sem Docker)
	@echo "🚀 Iniciando aplicação..."
	./mvnw spring-boot:run

run-prod: ## Executar aplicação em modo produção
	@echo "🚀 Iniciando aplicação em modo produção..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=production

# Testes
test: ## Executar testes unitários
	@echo "🧪 Executando testes unitários..."
	./mvnw test

test-integration: ## Executar testes de integração
	@echo "🧪 Executando testes de integração..."
	./mvnw verify

test-all: ## Executar todos os testes
	@echo "🧪 Executando todos os testes..."
	./mvnw clean verify

test-all-coverage: ## Executar todos os testes com relatório de cobertura
	@echo "🧪 Executando todos os testes com cobertura..."
	./mvnw clean test jacoco:report
	@echo "📊 Relatório de cobertura gerado em: target/site/jacoco/index.html"
	@echo "🌐 Para visualizar o relatório, abra o arquivo acima no seu navegador ou execute 'make coverage-open'"

test-coverage-check: ## Executar testes e verificar limites mínimos de cobertura  
	@echo "🧪 Executando testes e verificando limites de cobertura..."
	@echo "⚡ Limites configurados: 80% instruções, 70% branches"
	./mvnw clean test

# Build e empacotamento
build: ## Fazer build da aplicação
	@echo "🔨 Fazendo build da aplicação..."
	./mvnw clean package -DskipTests

build-with-tests: ## Fazer build com testes
	@echo "🔨 Fazendo build com testes..."
	./mvnw clean package

# Docker
docker-build: ## Fazer build da imagem Docker
	@echo "🐳 Fazendo build da imagem Docker..."
	docker build -t spreadsheet-generator:latest .

docker-run: ## Executar aplicação via Docker
	@echo "🐳 Executando aplicação via Docker..."
	docker run -p 8080:8080 --env-file .env spreadsheet-generator:latest

# Limpeza
clean: ## Limpar arquivos de build
	@echo "🧹 Limpando arquivos de build..."
	./mvnw clean
	@echo "✅ Limpeza concluída!"

# Utilitários
format: ## Formatar código
	@echo "💄 Formatando código..."
	./mvnw formatter:format

lint: ## Verificar qualidade do código
	@echo "🔍 Verificando qualidade do código..."
	./mvnw checkstyle:check

# Migração de banco (Hibernate Auto DDL)
db-create: ## Criar schema automaticamente (desenvolvimento)
	@echo "🗃️  Criando schema automaticamente (modo desenvolvimento)..."
	@echo "   ⚠️  ATENÇÃO: Isso vai recriar todas as tabelas!"
	./mvnw spring-boot:run -Dspring-boot.run.profiles=development -Dspring.jpa.hibernate.ddl-auto=create-drop -Dserver.port=8081 &
	@sleep 15
	@pkill -f spring-boot:run || true
	@echo "✅ Schema criado automaticamente!"

db-update: ## Atualizar schema automaticamente (produção)
	@echo "🗃️  Atualizando schema automaticamente..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=production -Dspring.jpa.hibernate.ddl-auto=update -Dserver.port=8081 &
	@sleep 15
	@pkill -f spring-boot:run || true
	@echo "✅ Schema atualizado automaticamente!"

db-validate: ## Validar schema atual
	@echo "🔍 Validando schema do banco..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=production -Dspring.jpa.hibernate.ddl-auto=validate -Dserver.port=8081 &
	@sleep 15
	@pkill -f spring-boot:run || true
	@echo "✅ Schema validado!"

db-reset: docker-down docker-up ## Reset do banco
	@echo "🔄 Resetando banco de dados..."
	@sleep 5
	@echo "✅ Banco resetado!"

# Logs
logs: ## Ver logs da aplicação
	@echo "📋 Exibindo logs..."
	tail -f logs/application.log

# Health check
health: ## Verificar saúde da aplicação
	@echo "🏥 Verificando saúde da aplicação..."
	@curl -s http://localhost:8080/actuator/health | jq '.' || echo "Aplicação não está rodando ou jq não está instalado"

# Informações do sistema
info: ## Mostrar informações do sistema
	@echo "ℹ️  Informações do sistema:"
	@echo "Java Version: $$(java -version 2>&1 | head -n 1)"
	@echo "Maven Version: $$(/usr/local/bin/mvn -version 2>/dev/null | head -n 1 || echo 'Maven não encontrado')"
	@echo "Docker Version: $$(docker --version 2>/dev/null || echo 'Docker não encontrado')"
	@echo "Docker Compose Version: $$(docker-compose --version 2>/dev/null || echo 'Docker Compose não encontrado')" 

# Compilação
clean-compile: ## Limpar e compilar
	@echo "🧹 Limpando e compilando..."
	./mvnw clean compile

# API e monitoramento
swagger: ## Abrir Swagger UI no browser
	@echo "📖 Abrindo Swagger UI..."
	@open http://localhost:8080/swagger-ui.html || echo "Abra http://localhost:8080/swagger-ui.html no seu browser"

actuator: ## Abrir Actuator endpoints no browser
	@echo "📊 Abrindo Actuator endpoints..."
	@open http://localhost:8080/actuator || echo "Abra http://localhost:8080/actuator no seu browser"

coverage-open: ## Abrir relatório de cobertura no browser
	@echo "📊 Abrindo relatório de cobertura..."
	@if [ -f target/site/jacoco/index.html ]; then \
		open target/site/jacoco/index.html || echo "Abra target/site/jacoco/index.html no seu browser"; \
	else \
		echo "❌ Relatório de cobertura não encontrado. Execute 'make test-all-coverage' primeiro."; \
	fi

coverage-summary: ## Mostrar resumo da cobertura no terminal
	@echo "📊 Resumo da cobertura de testes:"
	@if [ -f target/site/jacoco/jacoco.csv ]; then \
		echo ""; \
		echo "┌─────────────────────────────────────────────────────────────────────────┐"; \
		echo "│                           COBERTURA DE TESTES                          │"; \
		echo "├─────────────────────────────────────────────────────────────────────────┤"; \
		tail -n +2 target/site/jacoco/jacoco.csv | awk -F',' '{ \
			instruction_missed += $$4; \
			instruction_covered += $$5; \
			branch_missed += $$6; \
			branch_covered += $$7; \
			line_missed += $$8; \
			line_covered += $$9; \
		} END { \
			total_instructions = instruction_missed + instruction_covered; \
			instruction_coverage = (total_instructions > 0) ? (instruction_covered * 100 / total_instructions) : 0; \
			total_branches = branch_missed + branch_covered; \
			branch_coverage = (total_branches > 0) ? (branch_covered * 100 / total_branches) : 0; \
			total_lines = line_missed + line_covered; \
			line_coverage = (total_lines > 0) ? (line_covered * 100 / total_lines) : 0; \
			printf "│ Instruções:  %4d/%4d (%5.1f%%)                              │\n", instruction_covered, total_instructions, instruction_coverage; \
			printf "│ Branches:    %4d/%4d (%5.1f%%)                              │\n", branch_covered, total_branches, branch_coverage; \
			printf "│ Linhas:      %4d/%4d (%5.1f%%)                              │\n", line_covered, total_lines, line_coverage; \
		}'; \
		echo "└─────────────────────────────────────────────────────────────────────────┘"; \
		echo ""; \
		echo "🌐 Para ver detalhes completos: make coverage-open"; \
	else \
		echo "❌ Relatório de cobertura não encontrado. Execute 'make test-all-coverage' primeiro."; \
	fi
