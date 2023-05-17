package com.example.model.rest;

import com.example.model.order.Instrument;

import java.util.Collection;

public sealed interface InstrumentsResponse {
    record Instruments(Collection<Instrument> available) implements InstrumentsResponse {
    }

    record Other(RestResponse restResponse) implements InstrumentsResponse {
    }
}
