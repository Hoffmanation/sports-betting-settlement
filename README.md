# Sports Betting Settlement

A backend service for creating sports bets and asynchronously settling them against published event outcomes.

## Built with

**Java 25**, **Spring Boot 4**, **PostgreSQL**, **Redis**, **Flyway**, **Spring Data JPA**, **Kafka** (Redpanda locally), **MapStruct**, and **Lombok**.

## Features

- Place bets against an event and selected winner
- Publish event outcomes for asynchronous bet settlement
- Fetch a bet and inspect its `OPEN`, `WON`, or `LOST` status
- List bets with optional event filtering and `hasNext` slice pagination
- Cache bet reads and search results in Redis
- Document and explore the REST API with Swagger / OpenAPI
- Persist bets in PostgreSQL with Flyway-managed schema migrations
- Process bet creation commands and event outcomes through Kafka
- Exercise the full settlement producer/consumer flow with an in-process RocketMQ mock

## App Amenities/Services

- Command-first asynchronous bet creation: the API publishes a Kafka command rather than writing directly to the database
- Same-service Kafka consumers persist bet commands and process event outcomes
- PostgreSQL-backed bet storage with optimistic locking and atomic, once-only settlement
- Redis caching for individual bet reads and bet searches
- Virtual threads enabled for concurrent request handling
- Embedded Kafka and H2 for automated integration and repository tests
- Ten deterministic open sample bets seeded by Flyway for local exploration
- Docker Compose for local PostgreSQL, Redis, and Redpanda services

## Domain Rules

- A newly submitted bet starts in `OPEN` status
- Bets are created asynchronously; a bet may not be immediately available after the create request is accepted
- An event outcome is matched to open bets for the same event
- A bet is settled from `OPEN` to `WON` when its selected winner matches the outcome; otherwise it becomes `LOST`
- Settlement updates are atomic and only transition an open bet once
- Bet list pagination uses slices, returns `hasNext`, and does not calculate total counts

## Technology/Architectural Choices

- **Architecture**: single service with DDD-style packaging
- **API**: REST with controller interfaces and implementations
- **Async model**: Spring MVC, Java virtual threads, and Kafka command/event processing
- **Persistence**: PostgreSQL, Spring Data JPA, and Flyway
- **Caching**: Redis for bet details and search results
- **Settlement integration**: in-process RocketMQ mock broker and consumer
- **Mapping**: MapStruct between API, domain, and persistence models

## Project Structure

```text
src/main/java/com/sportsbetting
├── api
├── application
├── configuration
├── domain
├── integration
└── ports
```

## Run locally with Docker Compose

Prerequisites: Java 25, Maven 3.9+, Docker, and Docker Compose.

You can: 

- Start the application including all services from the project directory:


```bash
docker compose up -d
```
- Or start only infrastructure services from the project directory:

```bash
docker compose up -d postgres redis kafka
```

And then start the application locally:

```bash
mvn spring-boot:run
```

PostgreSQL data is retained in the `postgres_data` Docker volume. The API uses these local service defaults; see `src/main/resources/application.yml` to customize configuration.

Accessing Application:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Example API usage

### Place a bet

```bash
curl -X POST http://localhost:8080/api/v1/bets \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "user-1",
    "eventId": "match-42",
    "eventMarketId": "winner-market",
    "selectedWinnerId": "player-a",
    "amount": 10.00
  }'

# returns 202 Accepted with commandId, betId, commandType, and acceptedAt
```

The command is persisted asynchronously by a Kafka consumer. Wait for the bet to become visible before publishing its outcome.

### Publish an event outcome

```bash
curl -X POST http://localhost:8080/api/v1/event-outcomes \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "match-42",
    "eventName": "Final",
    "winnerId": "player-a"
  }'

# returns 202 Accepted after Kafka acknowledges the outcome
```

### Get a bet

```bash
curl http://localhost:8080/api/v1/bets/{betId}
```

### List bets

```bash
curl 'http://localhost:8080/api/v1/bets?eventId=match-42&page=0&size=20'
```

The `eventId` filter is optional. Pagination is zero-based, the maximum slice size is 100, and each response includes `hasNext` instead of total element/page counts.

## Kafka command and outcome flow

Bet creation and event outcomes are asynchronous. Both REST endpoints publish Kafka messages, and this service consumes them:

```text
POST /api/v1/bets
   ↓
Publish BET_CREATE_REQUESTED to sports-betting.bet-commands.v1
   ↓
Return 202 Accepted
   ↓
Bet-command consumer persists the bet to PostgreSQL

POST /api/v1/event-outcomes
   ↓
Publish outcome to event-outcomes
   ↓
Outcome consumer finds OPEN bets for the event
   ↓
RocketMQ mock producer publishes to bet-settlements
   ↓
RocketMQ mock consumer settles each bet in PostgreSQL
```

The assignment allows a mocked RocketMQ producer. This project supplies an in-process mock broker and consumer, so the settlement flow does not require a RocketMQ installation. Kafka-compatible Redpanda runs locally through Compose.

Local broker and topic details:

- Redpanda broker from the host: `localhost:19092`
- Redpanda broker from another Compose service: `kafka:9092`
- Bet command topic: `sports-betting.bet-commands.v1`
- Event outcome topic: `event-outcomes`
- Settlement mock topic: `bet-settlements`

Create the bets for an event and wait until they are visible before publishing its outcome. Bet commands and outcomes are consumed independently and may otherwise be processed in either order.

## Testing

Run all tests:

```bash
mvn test
```

Integration tests use embedded Kafka; repository tests use H2. The application itself uses PostgreSQL, and Hibernate validates (but does not create or update) the Flyway-managed schema. In particular, PostgreSQL supplies `created_at` using `DEFAULT now()`.

Run the PostgreSQL repository integration tests against a disposable PostgreSQL 17 Testcontainer (Docker must be running):

```bash
mvn -Ppostgres-it verify
```

These tests run separately from the default suite and validate the Flyway migrations, seeded bets, PostgreSQL-generated timestamps, slice queries, and once-only settlement updates against a real PostgreSQL database.
