package com.sportsbetting;

import com.sportsbetting.api.model.BetCommandAcceptedResponse;
import com.sportsbetting.application.service.BetApplicationService;
import com.sportsbetting.domain.event.BetCommandType;
import com.sportsbetting.domain.model.BetStatus;
import com.sportsbetting.ports.jpa.repository.BetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:integrationdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.cache.type=simple"
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
    partitions = 1,
    topics = {"event-outcomes", "sports-betting.bet-commands.v1"},
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class EventOutcomeFlowIntegrationTest {

    @Autowired
    private BetApplicationService betApplicationService;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Value("${local.server.port}")
    private int serverPort;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void publishesOutcomeThroughApiAndSettlesMatchingBets() throws Exception {
        String eventId = "match-" + UUID.randomUUID();
        var winningBet = createBet(eventId, "user-1", "player-a", "10.00");
        var losingBet = createBet(eventId, "user-2", "player-b", "5.00");
        await().atMost(Duration.ofSeconds(15))
            .until(() -> betRepository.existsById(winningBet.getBetId())
                && betRepository.existsById(losingBet.getBetId()));
        assertThat(betApplicationService.getBet(winningBet.getBetId()).status()).isEqualTo(BetStatus.OPEN);
        assertThat(betApplicationService.getBet(losingBet.getBetId()).status()).isEqualTo(BetStatus.OPEN);
        assertThat(betApplicationService.getBet(winningBet.getBetId()).status()).isEqualTo(BetStatus.OPEN);
        assertThat(betApplicationService.getBet(losingBet.getBetId()).status()).isEqualTo(BetStatus.OPEN);
        var cachedBets = betApplicationService.getBets(eventId, 0, 10);
        assertThat(cachedBets.content()).hasSize(2);
        assertThat(cachedBets.hasNext()).isFalse();
        var thirdBet = createBet(eventId, "user-3", "player-a", "7.00");
        await().atMost(Duration.ofSeconds(15)).until(() -> betRepository.existsById(thirdBet.getBetId()));
        assertThat(betApplicationService.getBet(thirdBet.getBetId()).status()).isEqualTo(BetStatus.OPEN);
        assertThat(betApplicationService.getBet(thirdBet.getBetId()).status()).isEqualTo(BetStatus.OPEN);
        assertThat(betApplicationService.getBets(eventId, 0, 10).content()).hasSize(3);
        assertThat(betApplicationService.getBets(eventId, 0, 2).hasNext()).isTrue();
        assertThat(betApplicationService.getBets(eventId, 1, 2).hasNext()).isFalse();

        var outcomeResponse = postJson("/api/v1/event-outcomes", """
            {
              "eventId": "%s",
              "eventName": "Final",
              "winnerId": "player-a"
            }
            """.formatted(eventId));
        assertThat(outcomeResponse.statusCode()).isEqualTo(202);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(betApplicationService.getBet(winningBet.getBetId()).status()).isEqualTo(BetStatus.WON);
            assertThat(betApplicationService.getBet(losingBet.getBetId()).status()).isEqualTo(BetStatus.LOST);
            assertThat(betApplicationService.getBet(thirdBet.getBetId()).status()).isEqualTo(BetStatus.WON);
            assertThat(betApplicationService.getBets(eventId, 0, 10).content())
                .allMatch(bet -> bet.status() != BetStatus.OPEN);
        });
    }

    @Test
    void exposesInformativeOpenApiForBetAndOutcomeEndpoints() throws Exception {
        var response = get("/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
            .contains("Sports Betting Settlement API")
            .contains("Publish an event outcome")
            .contains("List bets")
            .contains("CreateBetRequest")
            .contains("BetSliceResponse");

        var betsResponse = get("/api/v1/bets?page=0&size=2");
        assertThat(betsResponse.statusCode()).isEqualTo(200);
        assertThat(betsResponse.body())
            .contains("\"content\"")
            .contains("\"page\":0")
            .contains("\"size\":2")
            .contains("\"hasNext\"");
        assertThat(betsResponse.body()).doesNotContain("\"totalElements\"", "\"totalPages\"");

        assertThat(get("/api/v1/bets?page=-1&size=2").statusCode()).isEqualTo(400);
    }

    @Test
    void rejectsRequestsThatViolateDtoConstraints() throws Exception {
        var invalidBetResponse = postJson("/api/v1/bets", """
            {
              "userId": "user-1",
              "eventId": "match-42",
              "eventMarketId": "winner-market",
              "selectedWinnerId": "player-a",
              "amount": 0
            }
            """);
        assertThat(invalidBetResponse.statusCode()).isEqualTo(400);
        assertThat(invalidBetResponse.body())
            .contains("REQUEST_VALIDATION_FAILED")
            .contains("must be greater than 0");

        var invalidOutcomeResponse = postJson("/api/v1/event-outcomes", """
            {
              "eventId": " ",
              "eventName": "Championship Final",
              "winnerId": "player-a"
            }
            """);
        assertThat(invalidOutcomeResponse.statusCode()).isEqualTo(400);
        assertThat(invalidOutcomeResponse.body())
            .contains("REQUEST_VALIDATION_FAILED")
            .contains("must not be blank");
    }

    private BetCommandAcceptedResponse createBet(String eventId, String userId, String winnerId, String amount) throws Exception {
        var response = postJson("/api/v1/bets", """
            {
              "userId": "%s",
              "eventId": "%s",
              "eventMarketId": "winner-market",
              "selectedWinnerId": "%s",
              "amount": %s
            }
            """.formatted(userId, eventId, winnerId, amount));
        assertThat(response.statusCode()).isEqualTo(202);
        var accepted = jsonMapper.readValue(response.body(), BetCommandAcceptedResponse.class);
        assertThat(accepted.getCommandType()).isEqualTo(BetCommandType.BET_CREATE_REQUESTED.name());
        return accepted;
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + serverPort + path))
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + serverPort + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
