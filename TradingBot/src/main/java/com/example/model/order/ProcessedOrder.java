package com.example.model.order;

import java.time.Instant;

public sealed interface ProcessedOrder {
    record Bought(Instant created,
                  Identifier identifier,
                  Instrument instrument,
                  Offer offer) implements ProcessedOrder {
    }

    record Sold(Instant created,
                Identifier identifier,
                Instrument instrument,
                Offer offer) implements ProcessedOrder {
    }

    record Expired(Instant created,
                   Identifier identifier,
                   Instrument instrument) implements ProcessedOrder {
    }
}
