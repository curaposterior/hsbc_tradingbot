package com.example.platform;

import com.example.model.rest.*;
import com.example.model.security.Credentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Hackathon implements Platform {

    private static final String SERVER_URL = "https://backend.hsbc.redivivus.page/backend";
    private static final String PORTFOLIO_ENDPOINT = "/portfolio";
    private static final String BUY_ENDPOINT = "/buy";
    private static final String SELL_ENDPOINT = "/sell";
    private static final String INSTRUMENTS_ENDPOINT = "/instruments";
    private static final String SUBMITTED_ENDPOINT = "/submitted";
    private static final String PROCESSED_ENDPOINT = "/processed";
    private static final String HISTORY_ENDPOINT = "/history";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

    private final HttpRequest.Builder builder;

    public Hackathon(Credentials credentials) {
        final var client = credentials.client();
        final var password = credentials.password();
        final var basic = Base64.getEncoder().encodeToString((client.name() + ":" + password).getBytes(StandardCharsets.UTF_8));
        this.builder = HttpRequest
                .newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + basic);
    }


    @Override
    public PortfolioResponse portfolio() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + PORTFOLIO_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            final var statusCode = response.statusCode();
            return switch (statusCode) {
                case 200 -> objectMapper.readValue(body, PortfolioResponse.Portfolio.class);
                default -> new PortfolioResponse.Other(decode(statusCode, body));
            };
        } catch (IOException | InterruptedException e) {
            return new PortfolioResponse.Other(new RestResponse.Failed(e));
        }
    }

    private RestResponse decode(int statusCode, String body) throws JsonProcessingException {
        return switch (statusCode) {
            case 400 -> objectMapper.readValue(body, RestResponse.BadRequest.class);
            case 401 -> new RestResponse.Unauthorized(body);
            default -> new RestResponse.Unknown(statusCode, body);
        };
    }

    @Override
    public SubmitOrderResponse submit(SubmitOrderRequest order) {
        try {
            final var requestBody = writer.writeValueAsString(order);

            final var endpoint = switch (order) {
                case SubmitOrderRequest.Sell sell -> SELL_ENDPOINT;
                case SubmitOrderRequest.Buy buy -> BUY_ENDPOINT;
            };

            final var request = builder
                    .uri(URI.create(SERVER_URL + endpoint))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            final var statusCode = response.statusCode();
            return switch (statusCode) {
                case 200 -> objectMapper.readValue(body, SubmitOrderResponse.Acknowledged.class);
                case 400 -> objectMapper.readValue(body, SubmitOrderResponse.Rejected.class);
                default -> new SubmitOrderResponse.Other(decode(statusCode, body));
            };
        } catch (IOException | InterruptedException e) {
            return new SubmitOrderResponse.Other(new RestResponse.Failed(e));
        }
    }

    @Override
    public HistoryResponse history(HistoryRequest historyRequest) {
        try {
            final var requestBody = writer.writeValueAsString(historyRequest);

            final var request = builder
                    .uri(URI.create(SERVER_URL + HISTORY_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            final var statusCode = response.statusCode();
            return switch (statusCode) {
                case 200 -> objectMapper.readValue(body, HistoryResponse.History.class);
                default -> new HistoryResponse.Other(decode(statusCode, body));
            };
        } catch (IOException | InterruptedException e) {
            return new HistoryResponse.Other(new RestResponse.Failed(e));
        }
    }

    @Override
    public InstrumentsResponse instruments() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + INSTRUMENTS_ENDPOINT))
                    .GET()
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            final var statusCode = response.statusCode();
            return switch (statusCode) {
                case 200 -> objectMapper.readValue(body, InstrumentsResponse.Instruments.class);
                default -> new InstrumentsResponse.Other(decode(statusCode, body));
            };
        } catch (IOException | InterruptedException e) {
            return new InstrumentsResponse.Other(new RestResponse.Failed(e));
        }
    }

    @Override
    public SubmittedResponse submitted() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + SUBMITTED_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            final var statusCode = response.statusCode();
            return switch (statusCode) {
                case 200 -> objectMapper.readValue(body, SubmittedResponse.Submitted.class);
                default -> new SubmittedResponse.Other(decode(statusCode, body));
            };
        } catch (IOException | InterruptedException e) {
            return new SubmittedResponse.Other(new RestResponse.Failed(e));
        }
    }

    @Override
    public ProcessedResponse processed() {
        try {
            final var request = builder
                    .uri(URI.create(SERVER_URL + PROCESSED_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = response.body();
            final var statusCode = response.statusCode();
            return switch (statusCode) {
                case 200 -> objectMapper.readValue(body, ProcessedResponse.Processed.class);
                default -> new ProcessedResponse.Other(decode(statusCode, body));
            };
        } catch (IOException | InterruptedException e) {
            return new ProcessedResponse.Other(new RestResponse.Failed(e));
        }
    }
}
