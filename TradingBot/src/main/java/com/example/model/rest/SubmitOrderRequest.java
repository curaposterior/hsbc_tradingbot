package com.example.model.rest;

public sealed interface SubmitOrderRequest {
    record Sell(String symbol, String tradeId, long qty, long ask) implements SubmitOrderRequest {}
    record Buy(String symbol, String tradeId, long qty, long bid) implements SubmitOrderRequest {}
}
