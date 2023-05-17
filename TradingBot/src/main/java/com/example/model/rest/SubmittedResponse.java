package com.example.model.rest;

import com.example.model.order.SubmittedOrder;

import java.util.Collection;

public sealed interface SubmittedResponse {
    record Submitted(Collection<SubmittedOrder.Buy> buy, Collection<SubmittedOrder.Sell> sell) implements SubmittedResponse {
    }

    record Other(RestResponse restResponse) implements SubmittedResponse {
    }
}
