# Imagine Lanka Bank

A demo banking application built entirely on Spring (Spring Boot + Thymeleaf), showcasing integration with the
Tanzu Platform marketplace: **Tanzu Postgres**, **Tanzu RabbitMQ**, and the **Tanzu AI service** (`ai-models`).

Features: accounts dashboard, money transfers, bill pay, loan applications (auto-approved async), debit/credit
card management, an event-driven notifications feed powered by RabbitMQ, and an AI banking assistant chat widget
powered by the bound `ai-models` service.

Demo login: **kumara** / **Welcome11!**

## 1. Run locally

Requires Java 17+ and Docker. Maven is bundled via the wrapper (`./mvnw`) — no local Maven install needed.

```bash
docker compose up -d        # starts local Postgres + RabbitMQ
./mvnw clean package
java -jar target/imagine-lanka-bank.jar
```

Open http://localhost:8080. The AI chat widget will respond with a graceful fallback message locally since no
real AI credentials are configured — that's expected until you bind the `ai-models` service on Cloud Foundry.

To point the AI assistant at a real endpoint locally, export:
```bash
export SPRING_AI_OPENAI_API_KEY=sk-...
export SPRING_AI_OPENAI_BASE_URL=https://your-genai-proxy
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=your-model-name
```

## 2. Deploy to Tanzu Platform (Cloud Foundry)

Build the jar first:
```bash
./mvnw clean package
```

Create the three marketplace service instances (adjust instance names if you like):
```bash
cf create-service postgres on-demand-postgres-db ilb-postgres
cf create-service p.rabbitmq on-demand-plan ilb-rabbitmq
cf create-service ai-models reddye-llama32-plan ilb-ai
```

Push the app without starting it yet, so we can bind services first:
```bash
cf push --no-start
```

Bind each service to the app:
```bash
cf bind-service imagine-lanka-bank ilb-postgres
cf bind-service imagine-lanka-bank ilb-rabbitmq
cf bind-service imagine-lanka-bank ilb-ai
```

Start the app:
```bash
cf start imagine-lanka-bank
```

Postgres and RabbitMQ bindings are picked up automatically via `java-cfenv-boot` — no manual configuration
needed. Flyway will create the schema and seed demo data on first boot.

### If the AI chat widget doesn't respond after binding

The exact credential key names returned by the `genai-service` broker for the `ai-models` offering can vary.
The app tries common key names (`api_key`/`apiKey`, `api_base`/`url`/`endpoint`, `model`/`model_name`)
automatically. If it guesses wrong:

```bash
cf env imagine-lanka-bank          # inspect the real ai-models credentials JSON
cf set-env imagine-lanka-bank SPRING_AI_OPENAI_API_KEY <the-real-key>
cf set-env imagine-lanka-bank SPRING_AI_OPENAI_BASE_URL <the-real-base-url>
cf set-env imagine-lanka-bank SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL <the-real-model-name>
cf restart imagine-lanka-bank
```

## Architecture notes

- **Postgres**: Spring Data JPA + Flyway (`src/main/resources/db/migration`), schema and demo data (user `kumara`,
  two accounts, cards, a sample loan) seeded by `DataSeeder` on first boot.
- **RabbitMQ**: a `banking.events` topic exchange. Transfers, bill payments, and loan events are published as
  `BankingEvent` messages; a `@RabbitListener` consumer turns them into rows on the Notifications page —
  visible proof the async pipeline is working end to end.
- **AI service**: `spring-ai-openai-spring-boot-starter` provides the `ChatClient`; `AiVcapEnvironmentPostProcessor`
  (registered via `META-INF/spring.factories`) maps the bound `ai-models` service's credentials onto Spring AI's
  properties at startup. The assistant is grounded with the logged-in user's live account and transaction data.
