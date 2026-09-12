# teste-pratico-programador-java

Sistema de Pedidos Desktop Assíncrono

Este repositório contém dois projetos independentes:

- [`backend/`](backend) — serviço Spring Boot que expõe a API REST de pedidos e faz o consumo/processamento assíncrono via RabbitMQ (fila de entrada, DLQ, filas de status de sucesso/falha).
- [`desktop-gui/`](desktop-gui) — cliente desktop em Java Swing que consome a API do backend.

Cada projeto tem seu próprio `pom.xml` e Maven Wrapper, e é construído e executado separadamente:

```
cd backend
./mvnw spring-boot:run

cd desktop-gui
./mvnw package
java -jar target/desktop-gui.jar
```
