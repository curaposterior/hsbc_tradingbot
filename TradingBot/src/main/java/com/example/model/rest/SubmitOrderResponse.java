package com.example.model.rest;

import com.example.model.order.Identifier;

public sealed interface SubmitOrderResponse {
    record Acknowledged(Identifier tradeId) implements SubmitOrderResponse {
    }

    record Rejected(String becauseOf, Identifier tradeId) implements SubmitOrderResponse {
    }

    record Other(RestResponse restResponse) implements SubmitOrderResponse {
    }
}
