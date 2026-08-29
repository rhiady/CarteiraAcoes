# Carteira de Ações — Backend

API REST para cadastrar usuários, carteiras e corretoras, além de registrar compras e vendas. Uma ação nova é importada a partir do ticker durante uma compra; os dados cadastrais de uma corretora são importados pelo CNPJ e a corretora precisa constar na CVM.

## Arquitetura e domínios

O projeto usa Spring Boot, Spring Data JPA, Flyway, OpenFeign e PostgreSQL no perfil de desenvolvimento. A camada HTTP (`resources`) valida e recebe as requisições, os serviços aplicam as regras de negócio, os repositórios persistem os dados e os adaptadores/clients consultam provedores externos.

```text
Cliente HTTP → Resource → Service → Repository → Banco de dados
                         └→ Client/Adapter → APIs externas
```

Domínios principais:

- **Usuário e carteira:** um usuário pode ter carteiras para organizar as posições.
- **Ação e cotação:** uma compra pode criar a ação pelo ticker e mercado; nome da empresa e preço são buscados externamente.
- **Corretora:** o CNPJ identifica a instituição; razão social e endereço vêm da consulta cadastral e o registro é validado na CVM.
- **Operação e posição:** compras e vendas atualizam o histórico e as posições da carteira.

> A API ainda não possui autenticação/autorização. Não a exponha publicamente sem adicionar esse controle.

## Pré-requisitos

- JDK 21;
- PostgreSQL para executar com o perfil `dev`;
- uma chave da Alpha Vantage para ativos dos EUA e, quando necessário, um token da Brapi;
- Bash ou outro terminal capaz de exportar variáveis de ambiente.

O Maven Wrapper já está no repositório; não é necessário instalar Maven globalmente.

## Configuração local

Crie o banco local, por exemplo:

```sql
CREATE DATABASE carteira_acoes;
```

Copie `.env.example` para `.env` e use valores reais somente no seu ambiente local. O Spring Boot **não lê um arquivo `.env` sozinho** neste projeto; exporte as variáveis antes de iniciar a aplicação.

```bash
cp .env.example .env
set -a
source .env
set +a
./mvnw spring-boot:run
```

Exemplo seguro de `.env` (não versione chaves ou senhas):

```dotenv
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://localhost:5432/carteira_acoes
DB_USERNAME=postgres
DB_PASSWORD=<sua-senha-local>

# A aplicação atual acrescenta /quote/{ticker} a esta base.
BRAPI_URL=https://brapi.dev/api
BRAPI_TOKEN=<seu-token-brapi>

TWELVE_DATA_URL=https://api.twelvedata.com
TWELVE_DATA_API_KEY=<sua-chave-twelve-data>

BRASIL_API_URL=https://brasilapi.com.br/api

# Origens do frontend autorizadas a acessar a API no navegador.
# Use uma lista separada por vírgula e configure a origem implantada em produção.
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

O perfil `dev` usa PostgreSQL e aplica as migrations do Flyway. O perfil `test` usa H2 em memória e é selecionado ao executar os testes. `SPRING_PROFILES_ACTIVE` tem precedência sobre o perfil definido em arquivo de propriedades.

## Executar e testar

```bash
# Executa a suíte automatizada com o perfil de testes
./mvnw test

# Sobe a API localmente (porta 8080)
set -a && source .env && set +a
./mvnw spring-boot:run
```

Com a aplicação em execução:

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Console H2 (somente no perfil `test`): `http://localhost:8080/h2-console`

### Conexão do frontend

A URL base local da API é `http://localhost:8080` e os endpoints não usam um prefixo adicional: por exemplo, `GET http://localhost:8080/usuarios`.

Para o navegador liberar as chamadas, a origem exata em que o frontend está em execução deve constar em `CORS_ALLOWED_ORIGINS`. O padrão local permite os servidores de desenvolvimento `http://localhost:5173` (Vite) e `http://localhost:3000`. Para outra porta, hostname ou para produção, exporte a variável com as origens necessárias, separadas por vírgula, antes de iniciar a API:

```bash
export CORS_ALLOWED_ORIGINS="http://localhost:4200,https://app.exemplo.com"
./mvnw spring-boot:run
```

Origens não configuradas não recebem permissão CORS. Não use `*`, especialmente quando credenciais forem enviadas pelo navegador.

## API HTTP

As listagens aceitam paginação do Spring Data, por exemplo `?page=0&size=20&sort=id,desc`. A resposta paginada contém `content`, `totalElements`, `totalPages`, `size` e `number`.

| Recurso | Operações |
| --- | --- |
| Usuários | `POST /usuarios`, `GET /usuarios`, `GET /usuarios/{id}` |
| Carteiras | `POST /carteiras`, `GET /carteiras`, `GET /carteiras/{id}`, `GET /usuarios/{usuarioId}/carteiras`, `GET /carteiras/{id}/acoes` |
| Ações | `GET /acoes`, `GET /acoes/{id}`, `GET /acoes/ticker/{ticker}`, `POST /acoes/{id}/cotacao` |
| Corretoras | `POST /corretoras`, `GET /corretoras`, `GET /corretoras/{id}`, `GET /corretoras/cnpj/{cnpj}` |
| Operações | `POST /operacoes/compras`, `POST /operacoes/vendas`, `GET /operacoes/{id}`, `GET /operacoes/carteiras/{carteiraId}` |

### Corpos de criação

```json
POST /usuarios
{"nome":"Ana Silva","email":"ana@example.com","senha":"uma-senha-segura"}
```

```json
POST /carteiras
{"nome":"Longo prazo","usuarioId":1}
```

```json
POST /corretoras
{"cnpj":"00.000.000/0001-00"}
```

Não envie nome, endereço ou CEP da corretora: a API os consulta pelo CNPJ. O cadastro falha se a corretora não for localizada na base da CVM.

```json
POST /operacoes/compras
{"carteiraId":1,"acaoId":1,"quantidade":10,"precoUnitario":25.50,"corretagem":0,"impostos":0,"valorAdicional":0}

POST /operacoes/compras
{"carteiraId":1,"ticker":"PETR4","mercado":"BRASIL","quantidade":10,"precoUnitario":25.50,"corretagem":0,"impostos":0,"valorAdicional":0}

POST /operacoes/vendas
{"carteiraId":1,"acaoId":1,"quantidade":5,"corretagem":0,"impostos":0,"valorAdicional":0}
```

Em uma compra, informe exatamente `acaoId` (para ação existente) ou o par `ticker` e `mercado` (para localizar ou criar uma ação). `mercado` aceita `BRASIL` ou `EUA`; não envie nome da empresa nem preço para criar a ação. Os campos numéricos de custo são opcionais quando o contrato os permite. A quantidade deve ser maior que zero; em compra, `precoUnitario`, quando informado, também deve ser maior que zero.

### Erros

Falhas de validação retornam `400`, conflitos de integridade retornam `409` e erros inesperados retornam `500`. Regras de negócio retornam o status específico da regra. O formato é:

```json
{
  "timestamp": "2026-08-23T12:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Dados da requisição são inválidos.",
  "path": "/acoes"
}
```

Use a Swagger UI como fonte interativa dos schemas e das respostas detalhadas da versão em execução.

## Integrações externas

| Provedor | Uso na API | Configuração | Limites e falhas |
| --- | --- | --- | --- |
| [Brapi](https://brapi.dev/docs) | Dados e cotação de ações brasileiras | `BRAPI_URL`, `BRAPI_TOKEN` | O acesso varia por ticker, token e plano. A Brapi informa limites pelos headers e pode retornar `429`; reduza tentativas e respeite `Retry-After` quando disponível. |
| [Twelve Data](https://twelvedata.com/docs/discovery) | Cotação, nome e instante de ações dos EUA via `GET /quote` | `TWELVE_DATA_URL`, `TWELVE_DATA_API_KEY` | A cota depende da chave e do plano; uma consulta consome um crédito por símbolo. O serviço retorna `429` ao atingir o limite; aguarde antes de tentar novamente, sem loops de retry. |
| [BrasilAPI](https://brasilapi.com.br/docs) | Dados de CNPJ e lista de corretoras da CVM | `BRASIL_API_URL` | Não usa chave nesta integração. Faça consultas pontuais, trate indisponibilidade/retorno ausente e não faça varreduras ou loops sobre o serviço. |

O código atual da Brapi usa a rota legada `GET /api/quote/{ticker}` com token como parâmetro de consulta. A documentação atual do provedor apresenta rotas `v2` e recomenda enviar o token no header `Authorization`. Antes de atualizar a dependência/adapter, mantenha `BRAPI_URL` na base esperada pelo cliente atual; ao migrar para v2, ajuste o client e a configuração juntos.

Erros de provedores são tratados como falha de importação/atualização: confira a conectividade, a chave/token, o ticker/CNPJ e a disponibilidade do provedor antes de repetir a operação. Não registre tokens, senhas ou payloads sensíveis em logs.

## Segurança de configuração

- `.env` deve permanecer fora do Git; use apenas `.env.example` como modelo.
- Nunca inclua senhas, tokens ou chaves reais em documentação, issues ou commits.
- Revogue e substitua imediatamente qualquer credencial que tenha sido publicada por engano.
