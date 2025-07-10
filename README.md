# Spreadsheet Generator

Uma aplicação **Spring Boot** para processamento de dados com funcionalidades CRUD, geração de dados aleatórios no banco e exportação em CSV para S3, GCP e Local. Inclui documentação via Swagger.

### **Pré-requisitos**
- **Java**: `24.0.1` (OpenJDK)
- **Maven**: `3.9.10`
- **Docker & Docker Compose**: Essencial para o ambiente de desenvolvimento com PostgreSQL.

### **Instalação**
```bash
# Clonar repositório
git clone <repository-url>
cd file-stream-spreadsheet-generator

# Instalar dependências
make install

# Iniciar banco PostgreSQL
make docker-up

# Executar aplicação com hot-reload
make dev
```

### **Verificar Instalação**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## URLs Importantes

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **Aplicação** | http://localhost:8080 | Endpoint base da API |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentação interativa |
| **Actuator** | http://localhost:8080/actuator | Métricas e health checks |
| **Health Check** | http://localhost:8080/actuator/health | Status da aplicação |

## Comandos Make

### **Desenvolvimento**
```bash
# Iniciar desenvolvimento com hot-reload
make dev

# Desenvolvimento sem Docker
make dev-no-docker

# Executar aplicação
make run
```

### **Testes**
```bash
# Testes unitários
make test

# Testes de integração
make test-integration

# Todos os testes
make test-all
```

### **Banco de Dados**
```bash
# Iniciar PostgreSQL
make docker-up

# Parar PostgreSQL
make docker-down

# Ver logs do banco
make docker-logs

# Reset completo do banco
make db-reset
```

### **Utilitários**
```bash
# Build da aplicação
make build

# Build com testes
make build-with-tests

# Limpar arquivos de build
make clean

# Limpeza completa (build + Docker)
make clean-all

# Abrir Swagger no browser
make swagger

# Abrir Actuator no browser
make actuator

# Health check rápido
make health

# Ver informações do sistema
make info

# Ver todos os comandos
make help
```

### **Docker**
```bash
# Build da imagem Docker
make docker-build

# Executar via Docker
make docker-run

# Ver logs do PostgreSQL
make docker-logs
```

### **Qualidade de Código**
```bash
# Formatar código
make format

# Verificar qualidade do código
make lint

# Limpar e compilar
make clean-compile
```

### **Banco de Dados Avançado**
```bash
# Criar migration
make db-create

# Aplicar migration
make db-update

# Validar migration
make db-validate

# Resetar banco de dados
make db-reset
```

### **Logs e Monitoramento**
```bash
# Ver logs da aplicação
make logs

# Ver logs do PostgreSQL
make docker-logs

# Health check detalhado
make health
```

## Exemplos de Uso da API

### **Gerenciar Produtos**
```bash
# Listar produtos (paginado)
curl "http://localhost:8080/api/products?page=0&size=10"

# Criar produto
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Notebook Gamer", "description": "High-end gaming laptop", "price": 3499.99}'

# Buscar produto por ID
curl http://localhost:8080/api/products/{id}

# Filtrar por preço
curl "http://localhost:8080/api/products/filter/price?minPrice=100&maxPrice=500"
```

### **Exportar Dados**
```bash
# Exportar todos os produtos para CSV
curl -X POST http://localhost:8080/api/export/products/csv

# Download do arquivo gerado
curl -O http://localhost:8080/api/export/products/csv/download/{filename}
```

### **Gerar Dados de Teste**
```bash
# Gerar 1000 produtos aleatórios
curl -X POST http://localhost:8080/api/data-generator/products/1000

# Limpar todos os produtos
curl -X DELETE http://localhost:8080/api/data-generator/products/clear

# Ver informações do gerador
curl http://localhost:8080/api/data-generator/info
```

## Gerenciamento de Banco de Dados

### **Migrations com Flyway**

#### **Criar Nova Migration**
```bash
# Gerar timestamp
date +"%Y%m%d%H%M%S"

# Criar arquivo de migration
touch src/main/resources/db/migration/V{timestamp}__description.sql
```

#### **Aplicar Migrations**
```bash
# Ver status das migrations
./mvnw flyway:info

# Aplicar migrations pendentes
./mvnw flyway:migrate

# Validar migrations
./mvnw flyway:validate
```

#### **Exemplo de Migration**
```sql
-- V20250624120000__add_category_table.sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE products ADD COLUMN category_id UUID;
ALTER TABLE products ADD CONSTRAINT fk_products_category 
    FOREIGN KEY (category_id) REFERENCES categories(id);
```

### **Conectar ao Banco**
```bash
# Via Docker
docker exec -it spreadsheet-generator-db psql -U postgres -d spreadsheet_generator

# Via cliente local
psql -h localhost -U postgres -d spreadsheet_generator
```

## Configuração de Ambiente

### **Profiles Disponíveis**
- **`development`**: Configurações para desenvolvimento local
- **`test`**: Configurações para testes (H2 in-memory)
- **`production`**: Configurações otimizadas para produção

### **Variáveis de Ambiente**
```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/spreadsheet_generator
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=development
```

### **Executar com Profile Específico**
```bash
# Desenvolvimento
./mvnw spring-boot:run -Dspring.profiles.active=development

# Produção
./mvnw spring-boot:run -Dspring.profiles.active=production

# Porta customizada
./mvnw spring-boot:run -Dserver.port=8090
```

## Testes

### **Estrutura de Testes**
```
src/test/java/
├── service/
│   ├── ProductServiceTest.java      # Testes unitários
│   └── CsvExportServiceTest.java    # Testes unitários
└── controller/
    └── ProductControllerIT.java     # Testes de integração
```

### **Executar Testes**
```bash
# Todos os testes
mvn test

# Teste específico
mvn test -Dtest=ProductServiceTest

# Com relatório de cobertura
mvn test jacoco:report
```

## Hot-Reload (DevTools)

O projeto inclui **Spring Boot DevTools** para hot-reload automático durante o desenvolvimento.

### **Como Usar**
1. Execute `make dev` (não `make run`)
2. Edite qualquer arquivo `.java`
3. Salve o arquivo (`Ctrl+S`)
4. Aguarde ~3-5 segundos para o restart automático
5. Teste suas mudanças imediatamente!

### **Performance**
- **Mudança em método**: ~2-5 segundos
- **Nova classe**: ~3-7 segundos
- **Restart completo**: ~15-30 segundos (comparação)

### **LiveReload (Browser)**
- Instale a extensão LiveReload no seu browser
- Acesse a aplicação e ative a extensão
- Mudanças no código recarregam a página automaticamente

## Troubleshooting

### **Problemas Comuns**

#### **Aplicação não inicia**
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps

# Reiniciar PostgreSQL
make docker-down && make docker-up

# Verificar logs
docker-compose logs postgres
```

#### **Hot-reload não funciona**
```bash
# Usar comando correto
make dev  # (não make run)

# Verificar se DevTools está ativo nos logs
# Deve aparecer: "Devtools property defaults active!"

# Limpar e recompilar
mvn clean compile && make dev
```

#### **Testes falhando**
```bash
# Limpar cache Maven
./mvnw clean

# Executar teste específico
./mvnw test -Dtest=ProductServiceTest

# Verificar se está usando perfil correto
./mvnw test -Dspring.profiles.active=test
```

#### **Erro de conexão com banco**
```bash
# Verificar se porta está disponível
lsof -i :5432

# Verificar credenciais
psql -h localhost -U postgres -d spreadsheet_generator

# Recriar container do PostgreSQL
docker-compose down -v && docker-compose up -d postgres
```

## Contribuindo

1. **Fork** o repositório
2. **Crie** uma branch: `git checkout -b feature/nova-funcionalidade`
3. **Implemente** mudanças seguindo os padrões do projeto
4. **Execute** testes: `make test-all`
5. **Commit** com mensagem clara: `git commit -m "feat: adicionar nova funcionalidade"`
6. **Push** para a branch: `git push origin feature/nova-funcionalidade`
7. **Abra** um Pull Request

### **Padrões de Commit**
- `feat:` - Nova funcionalidade
- `fix:` - Correção de bug
- `docs:` - Mudanças na documentação
- `style:` - Formatação, ponto e vírgula faltando, etc
- `refactor:` - Mudança de código que não adiciona funcionalidade nem corrige bug
- `test:` - Adicionando testes
- `chore:` - Tarefas de manutenção

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

---

**Pronto para começar!** Execute `make dev` e acesse http://localhost:8080/swagger-ui.html para explorar a API! 