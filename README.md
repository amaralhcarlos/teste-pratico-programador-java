# teste-pratico-programador-java

Sistema de Pedidos Desktop Assíncrono

Este repositório contém dois projetos independentes:

- [`backend/`](backend) — serviço Spring Boot que expõe a API REST de pedidos e faz o consumo/processamento assíncrono via RabbitMQ (fila de entrada, DLQ, filas de status de sucesso/falha).
- [`desktop-gui/`](desktop-gui) — cliente desktop em Java Swing que consome a API do backend.

Cada projeto tem seu próprio `pom.xml` e Maven Wrapper, e é construído e executado separadamente.

## Arquitetura

```
desktop-gui  ──HTTP──▶  backend  ──publish──▶  RabbitMQ (fila de entrada)
                                                     │
                                                     ▼
                                          backend (consumer assíncrono)
                                                     │
                                        sucesso ◀────┴────▶ falha (DLQ)
                                          │                    │
                                          ▼                    ▼
                                  fila de status sucesso   fila de status falha
```

O `desktop-gui` cria o pedido via `POST /api/orders` e consulta o andamento por polling em `GET /api/orders/status/{id}`, cujo status é atualizado assim que o `backend` processa a mensagem consumida do RabbitMQ.

## Pré-requisitos

- JDK 17+
- Docker (para subir o RabbitMQ) ou uma instância própria do RabbitMQ
- [Bruno](https://www.usebruno.com/) (opcional, para testar a API manualmente)

## Como executar

### 1. Subir o RabbitMQ

A collection e o `docker-compose.yml` usados neste guia ficam em [`docs/`](docs).

```
docker compose -f docs/docker-compose.yml up -d
```

Isso disponibiliza o RabbitMQ em `localhost:5672` (AMQP) e o painel de gerenciamento em [`http://localhost:15672`](http://localhost:15672), com as credenciais `VR` / `software` (as mesmas já configuradas em `backend/src/main/resources/application.yaml`).

### 2. Rodar o backend

```
cd backend
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

### 3. Rodar o cliente desktop

```
cd desktop-gui
./mvnw package
java -jar target/desktop-gui.jar
```

## API

| Método | Rota                     | Descrição                                   |
|--------|--------------------------|----------------------------------------------|
| POST   | `/api/orders`            | Cria um pedido e retorna `202 Accepted` com o `id` gerado |
| GET    | `/api/orders/status/{id}`| Consulta o status de processamento do pedido (`RECEIVED`, `PROCESSING`, `SUCCESS` ou `FAILURE`) |

Mensagens de validação e de erro retornadas pela API ficam em português (PT-BR), já que são conteúdo exibido ao usuário final.

## Testando a API com Bruno

A collection do [Bruno](https://www.usebruno.com/) em [`docs/bruno`](docs/bruno) cobre a criação de pedidos válidos e os principais cenários de erro de validação (quantidade zero/negativa, produto vazio/em branco, UUID e data inválidos).

Para usar:

1. Abra o Bruno e importe a pasta `docs/bruno` como uma collection.
2. Selecione o environment `local` (`baseUrl: http://localhost:8080`).
3. Suba o RabbitMQ e o backend (passos acima) e execute as requisições.

## Testes automatizados

```
cd backend
./mvnw test
```
