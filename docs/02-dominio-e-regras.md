# Domínio e regras

## Linguagem ubíqua

| Termo | Significado |
| --- | --- |
| Recurso | Bem abstrato cuja capacidade é disputada pelos participantes. |
| Estoque | Contabilidade das unidades disponíveis, em hold e confirmadas de um recurso. |
| Fila | Configuração e ciclo operacional que controla o acesso a um recurso. |
| Participante | Identidade de sessão que disputa capacidade em uma fila. |
| Entrada | Registro ordenado da participação, incluindo a quantidade solicitada. |
| Posição | Quantidade de entradas `WAITING` anteriores; é uma fotografia, não uma promessa de tempo. |
| Worker | Processo interno periódico que tenta atender entradas em ordem. |
| Hold | Reserva temporária que retira unidades da disponibilidade até confirmação ou expiração. |
| Reserva | Agregado que representa o hold e seu resultado final. |
| Confirmação | Transição que torna definitivas as unidades mantidas pelo hold. |
| Idempotência | Garantia de que repetir a mesma intenção não duplica o efeito. |

Termos específicos como ingresso, sessão de cinema, pedido ou guichê não fazem
parte da linguagem do domínio.

## Contextos delimitados

### Identidade e Acesso

Controla gestores, credenciais e autorização das operações administrativas.
Participantes não são usuários globais desse contexto.

### Gestão de Filas

Controla configuração, associação ao recurso e ciclo de vida da fila. Não
calcula posição nem altera estoque diretamente.

### Sala de Espera

Controla entradas, ordem FIFO, consulta de posição e execução do worker. Decide
qual entrada deve ser tentada, mas delega a reserva de unidades.

### Inventário e Reservas

Controla o recurso, a contabilidade do estoque e o ciclo das reservas. É a
autoridade para responder se uma quantidade pode ser mantida em hold.

## Agregados e identidades

### Fila

Raiz do agregado de Gestão de Filas.

- Possui identificador global, nome, recurso associado, limite de unidades por
  participante, intervalo do worker, tamanho máximo do lote e estado.
- Uma fila referencia exatamente um recurso.
- Um recurso pode estar associado a somente uma fila não encerrada no MVP.

### Entrada

Raiz do agregado de Sala de Espera.

- Possui identificador global, fila, chave do participante, quantidade,
  sequência de chegada, instantes relevantes e estado.
- A sequência é monotônica dentro da fila e define o FIFO.
- Uma entrada atendida referencia a reserva criada.

### Estoque do recurso

Raiz do agregado de Inventário e Reservas.

- Possui identificador do recurso, quantidade total, disponível, em hold e
  confirmada.
- O estoque inicial é positivo e não é reposto no MVP.
- Alterações de quantidade e criação de hold são atômicas.

### Reserva

Pertence ao contexto Inventário e Reservas.

- Possui identificador, recurso, entrada de origem, quantidade, expiração,
  estado e instantes de transição.
- É criada diretamente em `HELD`; não existe reserva sem capacidade separada.

## Estados

### Fila

| Origem | Comando | Destino |
| --- | --- | --- |
| `DRAFT` | abrir | `OPEN` |
| `DRAFT` | encerrar | `CLOSED` |
| `OPEN` | pausar | `PAUSED` |
| `OPEN` | encerrar | `CLOSED` |
| `PAUSED` | retomar | `OPEN` |
| `PAUSED` | encerrar | `CLOSED` |

- `DRAFT`: pode ser configurada, mas não aceita entradas nem é processada.
- `OPEN`: aceita novas entradas e pode ser processada pelo worker.
- `PAUSED`: preserva entradas, mas rejeita novos ingressos e não avança o
  processamento.
- `CLOSED`: estado terminal. Rejeita ingressos e processamento.

Ao encerrar uma fila, um consumidor interno de `QueueClosed` cancela em lotes
as entradas ainda em `WAITING`. Entre o fechamento e esse processamento, elas
podem continuar visíveis como `WAITING`, mas não avançam. Holds já concedidos
conservam seu próprio prazo e ainda podem ser confirmados, cancelados ou
expirados.

### Entrada

| Origem | Resultado | Destino |
| --- | --- | --- |
| `WAITING` | hold criado | `HOLD_GRANTED` |
| `WAITING` | capacidade impossível | `UNFULFILLABLE` |
| `WAITING` | desistência ou fila encerrada | `CANCELLED` |
| `HOLD_GRANTED` | reserva confirmada | `CONFIRMED` |
| `HOLD_GRANTED` | reserva cancelada | `CANCELLED` |
| `HOLD_GRANTED` | prazo encerrado | `EXPIRED` |

- `WAITING`: aguarda processamento.
- `HOLD_GRANTED`: possui uma reserva `HELD`.
- `CONFIRMED`: a reserva foi confirmada.
- `EXPIRED`: o prazo do hold terminou sem confirmação.
- `UNFULFILLABLE`: a capacidade máxima ainda recuperável não atende a
  quantidade.
- `CANCELLED`: desistência do participante ou encerramento enquanto aguardava.

`CONFIRMED`, `EXPIRED`, `UNFULFILLABLE` e `CANCELLED` são terminais no MVP.

### Reserva

| Origem | Operação | Destino |
| --- | --- | --- |
| `HELD` | confirmar | `CONFIRMED` |
| `HELD` | cancelar | `CANCELLED` |
| `HELD` | expirar | `EXPIRED` |

Somente `HELD` aceita transição. Confirmação é definitiva no MVP.

## Invariantes

1. `total = available + held + confirmed`.
2. Nenhuma parcela do estoque pode ser negativa.
3. Uma reserva `HELD` reduz `available` e aumenta `held` na mesma transação.
4. Confirmar reduz `held` e aumenta `confirmed`.
5. Cancelar ou expirar reduz `held` e aumenta `available`.
6. A quantidade de uma entrada fica entre `1` e o limite da fila e não pode
   superar o total original do recurso.
7. O limite por participante configurado na fila não pode superar o total
   original do recurso.
8. Uma chave de participante possui no máximo uma entrada não terminal por
   fila.
9. Uma entrada possui no máximo uma reserva.
10. Somente filas `OPEN` aceitam entrada e processamento.
11. O worker considera entradas pela sequência crescente, nunca pelo instante
    do banco ou por identificador aleatório.

As regras de estoque devem ser protegidas também sob requisições e ciclos de
worker concorrentes.

## Ingresso e idempotência

O participante envia sua chave, quantidade e uma chave de idempotência.

- A primeira tentativa válida cria a entrada e um token secreto restrito a ela.
- Repetir a mesma chave de idempotência e o mesmo conteúdo retorna o resultado
  original.
- Reutilizar a chave de idempotência com conteúdo diferente é conflito.
- Tentar outra entrada enquanto já existe uma entrada não terminal para a mesma
  chave de participante também é conflito.
- Depois de um estado terminal, o participante pode entrar novamente enquanto
  a fila estiver `OPEN`, usando nova chave de idempotência.

O valor usado para autenticar o token deve ser armazenado apenas como hash. Para
que uma repetição idempotente devolva a resposta original, o registro temporário
de idempotência pode guardar a resposta cifrada até o fim da janela de
retenção. Token, resposta e chave de idempotência nunca são persistidos em
texto simples.

## Ciclo do worker

Para cada fila `OPEN`, um ciclo:

1. obtém um lote de entradas `WAITING` pela sequência;
2. inspeciona a primeira entrada ainda válida;
3. tenta criar atomicamente um hold com a quantidade solicitada;
4. ao criar, marca a entrada como `HOLD_GRANTED` e continua até o limite do
   lote;
5. se `available < requested` mas `available + held >= requested`, interrompe o
   lote e aguarda alguma confirmação, expiração ou cancelamento;
6. se `available + held < requested`, marca a entrada como `UNFULFILLABLE` e
   passa à próxima;
7. publica métricas e eventos somente após a confirmação da transação.

Assim, uma entrada posterior nunca é atendida enquanto uma anterior ainda
puder ser atendida pela liberação dos holds atuais. Uma entrada impossível é
concluída antes de o FIFO prosseguir.

Um ciclo concorrente não pode selecionar a mesma entrada nem consumir o mesmo
saldo. A implementação deve usar transação e bloqueio ou controle otimista com
nova tentativa; somente testes de concorrência definirão a estratégia concreta
como suficiente.

## Expiração

Um processo periódico de expiração localiza reservas `HELD` vencidas e, de
forma atômica:

1. muda a reserva para `EXPIRED`;
2. devolve a quantidade ao disponível;
3. muda a entrada correspondente para `EXPIRED`;
4. publica evento e métricas após o commit.

Expiração continua ativa independentemente do estado da fila.

Confirmar e expirar simultaneamente a mesma reserva deve produzir apenas um
resultado terminal.

## Eventos de domínio

Os eventos mínimos são:

- `QueueOpened`, `QueuePaused`, `QueueResumed`, `QueueClosed`;
- `EntryJoined`, `EntryCancelled`, `EntryUnfulfillable`;
- `HoldCreated`, `ReservationConfirmed`, `ReservationCancelled`,
  `ReservationExpired`.

No monólito, são eventos internos disparados após commit. Eles definem pontos de
integração para futuras extrações, mas não exigem broker nesta fase.
