# portfolio-catalog Specification

## Purpose

Permitir que o sistema mantenha os proprietários, carteiras e ações que formam a base consultável do acompanhamento de investimentos.

## Requirements

### Requirement: Cadastro e consulta de usuários
O sistema SHALL cadastrar usuários com nome, e-mail único e senha obrigatórios; a senha MUST NOT ser retornada por nenhuma API. O sistema SHALL expor criação, busca por identificador e listagem paginada de usuários.

#### Scenario: Cadastro válido de usuário
- **WHEN** um cliente envia nome, e-mail válido não utilizado e senha
- **THEN** o sistema cria o usuário e responde sem expor a senha

#### Scenario: E-mail duplicado
- **WHEN** um cliente cadastra um e-mail já existente
- **THEN** o sistema responde com erro de conflito ou validação padronizado e não cria outro usuário

### Requirement: Gestão de carteiras
O sistema SHALL criar carteiras nomeadas vinculadas a exatamente um usuário existente e SHALL permitir buscar uma carteira, listar carteiras e listar as carteiras de um usuário. Uma carteira MUST apresentar somente suas posições ativas ao ser consultada pelo recurso de posições.

#### Scenario: Criação para usuário existente
- **WHEN** um cliente cria uma carteira informando um usuário existente
- **THEN** o sistema persiste a carteira associada ao usuário

#### Scenario: Usuário inexistente
- **WHEN** um cliente cria ou consulta uma carteira usando usuário inexistente
- **THEN** o sistema responde com erro padronizado de recurso não encontrado

### Requirement: Catálogo de ações por mercado
O sistema SHALL consultar ações por identificador ou ticker e SHALL listar ações paginadamente. Uma ação SHALL ser criada exclusivamente como parte de uma compra válida para uma carteira existente; o sistema MUST NOT expor cadastro direto de ação. Na criação pela compra, o sistema SHALL receber ticker e mercado, importar nome da empresa e cotação atual do provedor compatível, e persistir ticker único em maiúsculas, mercado `BRASIL` ou `EUA` e a moeda derivada BRL ou USD. O sistema MUST rejeitar ticker não localizado ou resposta sem nome ou cotação válida sem persistir a ação.

#### Scenario: Criação de ação por compra
- **WHEN** uma compra válida informa ticker e mercado ainda não cadastrados
- **THEN** o sistema cria a ação com seus dados de mercado e a associa somente à posição criada ou atualizada para a carteira da compra

#### Scenario: Ação brasileira válida
- **WHEN** uma compra válida informa ticker do mercado BRASIL ainda não cadastrado
- **THEN** o sistema cria a ação com moeda BRL, ticker em maiúsculas, nome da empresa e cotação importados

#### Scenario: Cadastro direto indisponível
- **WHEN** um cliente solicita o cadastro direto de uma ação
- **THEN** o sistema rejeita a operação e não persiste ação sem uma compra vinculada a carteira

#### Scenario: Combinação de mercado e moeda inválida
- **WHEN** uma compra informa ticker inexistente ou o provedor não retorna dados válidos para o mercado
- **THEN** o sistema rejeita a compra sem persistir ação, posição ou operação

### Requirement: Posições ativas da carteira
O sistema SHALL listar as ações de uma carteira apenas quando a quantidade da posição for maior que zero. Quantidades e valores monetários MUST ser representados com precisão decimal, sem valores de ponto flutuante.

#### Scenario: Posição zerada não é exibida
- **WHEN** uma carteira contém uma posição com quantidade igual a zero
- **THEN** a consulta de posições ativas não inclui essa posição
