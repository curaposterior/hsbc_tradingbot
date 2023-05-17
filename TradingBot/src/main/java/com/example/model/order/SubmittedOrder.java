package com.example.model.order;

import java.time.Instant;

public sealed interface SubmittedOrder {
    record Buy(Instant created,
               Identifier identifier,
               Instrument instrument,
               Offer bid) implements SubmittedOrder {
    }

    record Sell(Instant created,
                Identifier identifier,
                Instrument instrument,
                Offer ask) implements SubmittedOrder {
    }

}
