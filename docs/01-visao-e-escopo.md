# Visão e escopo

## Contexto

O TCC investigará como diferentes níveis de granularidade de microserviços
afetam o comportamento de um sistema sob carga. Para que a comparação seja
válida, todas as variantes partirão do mesmo domínio, das mesmas regras e de um
contrato funcional equivalente.

O sistema de referência será primeiro implementado como monólito modular. Essa
forma oferece uma linha de base executável e permite explicitar fronteiras de
negócio antes de introduzir comunicação distribuída.

## Problema do produto

Quando muitas pessoas disputam um recurso limitado, permitir acesso concorrente
sem coordenação causa sobrecarga, disputa injusta e reservas acima da
capacidade. O produto deve:

- ordenar participantes de forma previsível;
- limitar o ritmo de acesso ao estoque;
- reservar quantidades sem exceder a capacidade;
- devolver capacidade de reservas não concluídas;
- permitir que gestores controlem o ciclo da fila;
- expor dados operacionais suficientes para acompanhar e comparar execuções.

O recurso é propositalmente abstrato. Pode representar vagas, unidades,
licenças ou qualquer capacidade contável, mas o sistema não conhece a atividade
que ocorre depois da confirmação.

## Objetivos

### Objetivo do produto

Fornecer uma API de sala de espera que coordene, em ordem FIFO, a criação de
reservas temporárias sobre quantidades limitadas de um recurso genérico.

### Objetivo acadêmico

Obter uma linha de base modular e instrumentada que permita, posteriormente,
comparar variantes com diferentes granularidades de serviço usando
principalmente:

- latência e throughput;
- taxa de erros;
- tempo de espera e profundidade das filas;
- uso de CPU e memória;
- comportamento de reservas e expirações.

Os cenários concretos de decomposição não pertencem a esta fase. Primeiro, as
fronteiras e o comportamento do monólito devem ficar estáveis.

## Atores

### Gestor

Usuário autenticado que cadastra um recurso, cria uma fila, define seus limites,
acompanha o estado e executa transições de abertura, pausa, retomada e
encerramento.

### Participante

Cliente da sala de espera. Não precisa manter uma conta global. Entra em uma
fila com um identificador de sessão e uma quantidade desejada, recebe um token
restrito àquela entrada e consulta seu progresso até obter ou não uma reserva.

### Worker

Componente interno que processa periodicamente as filas abertas. Ele respeita a
ordem FIFO e solicita a criação de holds ao módulo de Inventário e Reservas.

### Operador da plataforma

Responsável técnico que observa saúde, logs e métricas. Não é um papel do
domínio nem substitui o gestor da fila.

## Capacidades do MVP

- autenticar gestores;
- criar e consultar recursos com estoque inicial;
- criar e consultar filas associadas a recursos;
- abrir, pausar, retomar e encerrar filas;
- manter no máximo uma entrada não terminal por participante em cada fila
  aberta;
- consultar posição aproximada e estado de uma entrada;
- cancelar uma entrada ou um hold ainda não confirmado;
- processar entradas em lotes por um worker periódico;
- criar hold com prazo de expiração;
- confirmar, cancelar ou expirar o hold;
- impedir reserva acima do estoque;
- correlacionar requisições, registrar logs e publicar métricas.

## Fora do escopo do MVP

- interface web ou aplicativo cliente;
- venda, pagamento, entrega ou uso do recurso confirmado;
- contexto específico de shows, cinema, atendimento ou agendamento;
- notificações por e-mail, SMS ou push;
- compartilhamento do mesmo estoque por várias filas ativas;
- múltiplas organizações ou isolamento formal de tenants;
- reposição de estoque após a criação do recurso;
- políticas de prioridade, sorteio ou pré-fila;
- cancelamento de uma reserva já confirmada;
- implantação em microserviços.

Esses itens só devem entrar após nova decisão explícita, pois alteram o domínio
ou as condições do experimento.

## Critérios de sucesso do monólito

O monólito estará pronto para ser a linha de base quando:

1. o fluxo completo de criação do recurso até confirmação ou expiração do hold
   estiver automatizado e testado;
2. concorrência não permitir estoque negativo ou duplicidade de entrada;
3. os módulos respeitarem suas fronteiras mesmo compartilhando processo e
   banco;
4. métricas permitirem reconstruir carga, espera, processamento e resultado;
5. uma carga repetível puder ser executada sem depender de interface humana;
6. contratos e regras estiverem sincronizados com esta documentação.

## Restrições

- Java 21 e Spring Boot são a base tecnológica atual.
- O monólito usa um processo e um banco MySQL.
- APIs públicas usam HTTP e JSON.
- A política inicial é FIFO e não pode ser enfraquecida silenciosamente para
  melhorar throughput.
- A implementação distribuída não deve ser antecipada dentro do monólito com
  HTTP interno ou broker sem necessidade funcional.
