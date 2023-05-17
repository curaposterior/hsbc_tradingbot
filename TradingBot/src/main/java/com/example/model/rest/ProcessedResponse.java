package com.example.model.rest;

import com.example.model.order.ProcessedOrder;

import java.util.Collection;

public sealed interface ProcessedResponse {
    record Processed(Collection<ProcessedOrder.Bought> bought, Collection<ProcessedOrder.Sold> sold,
                     Collection<ProcessedOrder.Expired> expired) implements ProcessedResponse {
    }

    record Other(RestResponse restResponse) implements ProcessedResponse {
    }
}
