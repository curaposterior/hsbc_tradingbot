package com.example;

import com.example.model.order.Instrument;
import com.example.model.order.ProcessedOrder;
import com.example.model.order.SubmittedOrder;
import com.example.model.rest.*;
import com.example.model.security.Credentials;
import com.example.platform.Hackathon;
import com.example.platform.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.*;

public class TradingBot {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);

    public static void main(String[] args) {
        logger.info("Starting the application");
        try (final var credentialsResource = TradingBot.class.getResourceAsStream("/credentials/default.json")) {
            final var credentials = objectMapper.readValue(credentialsResource, Credentials.class);
            Platform platfrm = new Hackathon(credentials);

            final var patrickBateman = new BateManBot(platfrm);

            ScheduledExecutorService  executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(patrickBateman, 0, 10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            logger.error("Something bad happened", exception);
        }
    }
}
