package com.example.model.rest;

import com.example.model.order.ProcessedOrder;

import java.util.Collection;

public sealed interface HistoryResponse {
    record History(Collection<ProcessedOrder.Bought> bought, Collection<ProcessedOrder.Sold> sold) implements HistoryResponse {
    }

    record Other(RestResponse restResponse) implements HistoryResponse {
    }
}
