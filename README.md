# Order Service - Ambev Challenge

Este projeto implementa o serviço `order`, responsável por receber pedidos do Produto Externo A, calcular o valor total dos produtos, e disponibilizar os dados processados para o Produto Externo B.

## 📦 Tecnologias Utilizadas

- Java 21 + Spring Boot
- MongoDB (armazenamento dos pedidos)
- Redis (cache de leitura)
- Kafka (mensageria para integração)
- Prometheus + Grafana (monitoramento e métricas)
- Docker + Docker Compose (orquestração de containers)

## 🚀 Subindo a aplicação

```bash
docker-compose up --build
```

A aplicação estará disponível em:

- API: [http://localhost:8080](http://localhost:8080)
- Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Mongo Express: [http://localhost:8081](http://localhost:8081)
- Kafdrop: [http://localhost:9000](http://localhost:9000)
- Prometheus: [http://localhost:9090](http://localhost:9090)
- Grafana: [http://localhost:3000](http://localhost:3000) (Login: admin/admin)

## 🧪 Endpoints

- `POST /orders`: recebe um pedido no seguinte formato:
```json
{
  "orderId": "12345",
  "items": [
    { "productCode": "P1", "quantity": 2, "unitPrice": 10.0 },
    { "productCode": "P2", "quantity": 1, "unitPrice": 15.5 }
  ]
}
```

- `GET /orders/{orderId}`: consulta o pedido com os produtos e valor total.

## 📊 Métricas

A aplicação expõe métricas no formato Prometheus em:

```
http://localhost:8080/actuator/prometheus
```

O Grafana carrega automaticamente um dashboard com:
- Tempo de resposta das requisições
- Quantidade de requisições
- Percentual de erro por status HTTP

## ✅ Rodando os testes

### Testes unitários e de integração

```bash
./gradlew clean test
```

Os relatórios de teste são gerados em:

```
build/reports/tests/test/index.html
```

### Testes com containers (Testcontainers)

O projeto utiliza Testcontainers para testes de integração com MongoDB, Redis e Kafka em containers. Eles são executados automaticamente com os testes do Gradle.

Certifique-se de que o Docker está rodando.

## 📁 Estrutura do projeto

```
├── src
│   ├── main
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   └── dto
├── Dockerfile
├── docker-compose.yml
├── grafana/
├── prometheus.yml
└── README.md
```

## 🧠 Considerações técnicas

- Os pedidos duplicados são identificados e ignorados com base no `orderId`.
- O sistema foi projetado para suportar o recebimento de 150 mil a 200 mil pedidos por dia.
- Segue boas práticas de design, como:
  - Separação entre controller, service e repository
  - Cache para otimizar leitura
  - Observabilidade integrada desde o início

---

> Projeto desenvolvido para o desafio técnico da Ambev.
