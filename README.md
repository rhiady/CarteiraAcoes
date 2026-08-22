# Carteira de Ações — Backend

API para acompanhamento de carteiras de ações brasileiras e americanas.

## Configuração local

1. Copie `.env.example` para `.env` e preencha somente as variáveis necessárias ao ambiente local.
2. Crie o banco PostgreSQL indicado por `DB_URL` (o padrão é `carteira_acoes`).
3. Execute `./mvnw spring-boot:run`.

O arquivo `.env` contém credenciais e não é versionado. O profile de teste usa H2 e não depende de provedores externos reais.
