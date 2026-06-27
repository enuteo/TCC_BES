# API conceitual

## Natureza deste documento

Este é o contrato funcional alvo do MVP, não uma descrição dos endpoints já
disponíveis. Ele fixa recursos, operações e semântica necessária para orientar
a implementação e a geração de carga. Nomes finais de todos os campos, limites
numéricos e o documento OpenAPI serão fechados junto ao código.

O prefixo alvo é `/api/v1`, com JSON em UTF-8.

## Convenções

### Identificadores e tempo

- Identificadores públicos são opacos e globalmente únicos.
- Instantes são representados em UTC no formato ISO 8601.
- Quantidades são inteiros positivos.
- Estados usam os nomes canônicos definidos em
  [Domínio e regras](02-dominio-e-regras.md).

### Correlação

O cliente pode enviar `X-Correlation-ID`. Se estiver ausente ou inválido, a API
gera um valor. A resposta sempre devolve o identificador, que também acompanha
logs e erros.

### Autenticação

- Operações de gestor usam uma credencial Bearer emitida por Identidade e
  Acesso.
- Criar uma entrada é público enquanto a fila está aberta.
- A criação devolve uma única vez um token Bearer restrito à entrada.
- O token da entrada autoriza consulta e cancelamento da própria entrada e
  consulta, confirmação ou cancelamento do hold associado.
- O gestor proprietário pode consultar e controlar os recursos sob sua
  responsabilidade.

O mecanismo concreto da credencial de gestor será fechado ao implementar
Identidade e Acesso. A autorização descrita aqui não depende de JWT.

### Idempotência

Operações de criação e comandos repetíveis aceitam `Idempotency-Key`.

- Mesma chave, mesma operação, mesmo ator — gestor ou par fila/participante — e
  mesmo conteúdo retornam o resultado original.
- A mesma chave com conteúdo diferente retorna `409 Conflict`.
- O servidor define uma janela de retenção maior que o maior timeout esperado
  dos clientes e a documenta no OpenAPI.
- A idempotência não substitui a regra de uma entrada ativa por participante.
- Quando o resultado original contém o token da entrada, ele pode ser mantido
  cifrado somente durante essa janela para permitir repetição segura.

### Paginação

Listagens administrativas usam cursor opaco e limite. A ordenação padrão deve
ser estável; entradas são ordenadas por sequência de chegada.

## Identidade e saúde

| Método e rota | Acesso | Capacidade |
| --- | --- | --- |
| `POST /api/v1/auth/login` | Público | Autenticar gestor e emitir credencial. |
| `GET /api/status` | Público | Endpoint técnico atual de status; será mantido apenas por compatibilidade enquanto necessário. |
| `GET /actuator/health` | Operação | Saúde técnica da aplicação. |
| `GET /actuator/prometheus` | Operação | Coleta de métricas pelo Prometheus. |

`/api/auth/login` é legado do protótipo. Não deve ser apresentado como
autenticação segura nem receber novas dependências de domínio.

## Recursos e estoque

| Método e rota | Acesso | Capacidade |
| --- | --- | --- |
| `POST /api/v1/resources` | Gestor | Criar recurso com nome e quantidade total inicial. |
| `GET /api/v1/resources` | Gestor | Listar recursos próprios. |
| `GET /api/v1/resources/{resourceId}` | Gestor | Consultar total, disponível, em hold e confirmado. |

Criar um recurso exige `Idempotency-Key`. O estoque não pode ser editado ou
reposto no MVP. O recurso associado a uma fila ainda não encerrada não pode ser
associado a outra.

## Filas

| Método e rota | Acesso | Capacidade |
| --- | --- | --- |
| `POST /api/v1/queues` | Gestor | Criar fila em `DRAFT`. |
| `GET /api/v1/queues` | Gestor | Listar filas próprias. |
| `GET /api/v1/queues/{queueId}` | Gestor | Consultar configuração, estado e contadores. |
| `POST /api/v1/queues/{queueId}/open` | Gestor | Abrir fila em `DRAFT`. |
| `POST /api/v1/queues/{queueId}/pause` | Gestor | Pausar fila `OPEN`. |
| `POST /api/v1/queues/{queueId}/resume` | Gestor | Retomar fila `PAUSED`. |
| `POST /api/v1/queues/{queueId}/close` | Gestor | Encerrar definitivamente e iniciar o cancelamento das esperas. |

A criação recebe, no mínimo:

- nome;
- `resourceId`;
- quantidade máxima por participante;
- duração do hold;
- intervalo do worker;
- tamanho máximo do lote.

Abrir valida a configuração e a exclusividade do recurso. Comandos repetidos
que já atingiram exatamente o estado solicitado devolvem a representação atual;
qualquer outra transição inválida retorna conflito.

Encerrar muda a fila imediatamente para `CLOSED` e responde `202 Accepted`
enquanto um consumidor interno cancela entradas `WAITING` em lotes. Repetir o
comando depois da conclusão devolve a representação atual.

## Entradas da sala de espera

| Método e rota | Acesso | Capacidade |
| --- | --- | --- |
| `POST /api/v1/queues/{queueId}/entries` | Público | Ingressar em uma fila aberta. |
| `GET /api/v1/entries/{entryId}` | Token da entrada ou gestor | Consultar estado e posição. |
| `DELETE /api/v1/entries/{entryId}` | Token da entrada ou gestor | Cancelar espera ou hold ainda ativo. |

O ingresso exige `Idempotency-Key` e recebe:

- `participantKey`: identificador opaco fornecido pelo cliente;
- `quantity`: quantidade desejada.

Uma criação bem-sucedida responde `201 Created` com:

- identificador e estado da entrada;
- quantidade;
- posição atual;
- instante de criação;
- token secreto da entrada.

O token completo só aparece nessa resposta. Consultas posteriores apresentam
posição apenas em `WAITING`; em `HOLD_GRANTED`, também apresentam identificador
da reserva e prazo para confirmação. Estados terminais não apresentam posição.

Cancelar `WAITING` ou `HOLD_GRANTED` é idempotente e devolve o estado terminal.
Tentar cancelar uma entrada confirmada ou expirada retorna conflito.

## Reservas

| Método e rota | Acesso | Capacidade |
| --- | --- | --- |
| `GET /api/v1/reservations/{reservationId}` | Token da entrada ou gestor | Consultar quantidade, estado e expiração. |
| `POST /api/v1/reservations/{reservationId}/confirm` | Token da entrada | Confirmar hold. |
| `DELETE /api/v1/reservations/{reservationId}` | Token da entrada ou gestor | Cancelar hold. |

Confirmação exige `Idempotency-Key`. Repetir a confirmação já concluída retorna
o resultado original. Confirmar ou cancelar depois que outra transição terminal
venceu a disputa retorna `409 Conflict` com o estado atual.

Não existe endpoint público para fazer o worker avançar. O processamento e a
expiração são jobs internos, evitando que a correção da fila dependa de polling
dos participantes.

## Respostas e erros

### Sucesso

- `200 OK`: consulta ou comando concluído com representação.
- `201 Created`: recurso, fila ou entrada criada.
- `202 Accepted`: fila encerrada, com cancelamento das entradas aguardando ainda
  em processamento.
- `204 No Content`: exclusão lógica idempotente quando não for necessário
  devolver representação.

### Erro

- `400 Bad Request`: JSON inválido, campo ausente ou formato incorreto.
- `401 Unauthorized`: credencial ausente ou inválida.
- `403 Forbidden`: credencial válida sem acesso ao recurso.
- `404 Not Found`: recurso inexistente ou não visível ao solicitante.
- `409 Conflict`: transição inválida, duplicidade, corrida perdida ou
  idempotência incompatível.
- `422 Unprocessable Entity`: valor bem formado que viola limite de domínio.
- `500 Internal Server Error`: falha inesperada sem exposição de detalhes.

O corpo de erro alvo contém:

```json
{
  "timestamp": "2026-06-26T12:00:00Z",
  "status": 409,
  "code": "QUEUE_INVALID_TRANSITION",
  "message": "A fila não pode ser aberta no estado atual.",
  "path": "/api/v1/queues/example/open",
  "correlationId": "opaque-value",
  "fieldErrors": []
}
```

`code` é estável e adequado a clientes; `message` é legível e pode evoluir.
Erros não revelam existência de recursos de outro gestor, credenciais, tokens
ou stack traces.

## Compatibilidade

Antes de congelar a linha de base:

- publicar OpenAPI derivado da implementação;
- testar exemplos e códigos de erro;
- definir limites e retenção de idempotência;
- preservar `/api/v1` durante todos os experimentos de granularidade;
- tratar mudança de regra ou contrato como mudança da linha de base, não como
  detalhe de uma variante.
