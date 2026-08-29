# portfolio-operations Specification

## Purpose

Registrar compras e vendas como histórico imutável e manter a posição corrente de cada ação em uma carteira sempre consistente com esse histórico.

## Requirements

### Requirement: Registro de compra
O sistema SHALL registrar uma compra somente para carteira existente quando a quantidade for positiva. A compra SHALL identificar uma ação existente por `acaoId` OU informar conjuntamente `ticker` e `mercado`; MUST rejeitar identificação ausente, combinação parcial de ticker/mercado ou as duas formas de identificação na mesma requisição. Quando `acaoId` for informado, a ação deverá existir. Quando ticker e mercado forem informados, o sistema SHALL reutilizar a ação correspondente se ela existir ou criá-la a partir do provedor de mercado, dentro da mesma transação da compra. Se o preço unitário for informado, SHALL usá-lo; se estiver ausente, SHALL obter cotação válida externamente. A compra MUST criar ou aumentar somente a posição da ação na carteira informada e registrar os valores bruto e líquido calculados.

#### Scenario: Compra de ação existente por identificador
- **WHEN** um cliente compra quantidade positiva para uma carteira existente informando `acaoId` válido e preço unitário válido
- **THEN** o sistema registra a compra e aumenta somente a posição daquela carteira sem consultar cotação externa

#### Scenario: Compra com preço informado
- **WHEN** um cliente compra quantidade positiva de uma ação existente com preço unitário válido
- **THEN** o sistema registra a compra e aumenta a posição sem consultar cotação externa

#### Scenario: Compra cria ação e posição
- **WHEN** um cliente compra quantidade positiva para uma carteira existente informando ticker e mercado ainda não cadastrados
- **THEN** o sistema importa a ação, cria sua posição naquela carteira e registra a compra na mesma transação

#### Scenario: Compra reutiliza ação por ticker
- **WHEN** um cliente compra informando ticker e mercado de uma ação já cadastrada
- **THEN** o sistema reutiliza a ação existente e altera somente a posição da carteira informada

#### Scenario: Identificação de ação inválida
- **WHEN** uma compra informa `acaoId` junto com ticker/mercado, omite todos esses campos ou informa apenas um entre ticker e mercado
- **THEN** o sistema responde com erro de validação e não persiste ação, posição ou operação

#### Scenario: Compra sem preço e cotação indisponível
- **WHEN** um cliente compra sem informar preço e a cotação não pode ser obtida
- **THEN** o sistema não registra operação nem altera a posição

### Requirement: Registro de venda
O sistema SHALL registrar uma venda somente para posição existente cuja quantidade seja suficiente. O preço unitário da venda MUST ser obtido por cotação externa; o cliente MUST NOT defini-lo. A venda SHALL reduzir a posição sem removê-la, inclusive quando resultar em quantidade zero.

#### Scenario: Venda total
- **WHEN** um cliente vende exatamente toda a quantidade de uma posição
- **THEN** o sistema registra a venda e mantém a posição persistida com quantidade zero

#### Scenario: Venda acima da posição
- **WHEN** um cliente solicita venda com quantidade maior que a posição atual
- **THEN** o sistema responde 422 e não cria operação nem altera a posição

### Requirement: Cálculos e valores de operação
O sistema SHALL calcular valor bruto como quantidade vezes preço unitário e valor líquido como valor bruto mais valor adicional menos corretagem e impostos. Corretagem, impostos e valor adicional ausentes MUST assumir zero, e custos não podem ser negativos.

#### Scenario: Cálculo com custos opcionais ausentes
- **WHEN** uma compra é registrada sem corretagem, impostos e valor adicional
- **THEN** o sistema registra esses campos como zero e calcula valor líquido igual ao valor bruto

### Requirement: Atomicidade e histórico imutável
O sistema SHALL aplicar cada compra ou venda atomicamente: operação e posição MUST ser persistidas juntas, ou nenhuma mudança poderá persistir. Operações registradas MUST NOT possuir endpoint de edição genérica; o histórico de uma carteira SHALL ser retornado paginado em ordem decrescente de data/hora.

#### Scenario: Falha durante venda
- **WHEN** uma venda falha depois de iniciar o processamento, inclusive por falha de cotação
- **THEN** a transação é revertida e a posição e o histórico permanecem inalterados

#### Scenario: Histórico ordenado
- **WHEN** um cliente consulta as operações de uma carteira
- **THEN** o sistema retorna resultados paginados da operação mais recente para a mais antiga

### Requirement: Concorrência de posição
O sistema SHALL impedir atualização perdida quando operações concorrentes alterarem a mesma posição e MUST preservar a regra de que a quantidade jamais seja negativa.

#### Scenario: Operações concorrentes incompatíveis
- **WHEN** duas operações concorrentes tentam gravar estados conflitantes para a mesma posição
- **THEN** o sistema rejeita ou repete de forma controlada a atualização conflitante sem persistir quantidade incorreta
