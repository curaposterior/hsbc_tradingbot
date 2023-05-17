package com.example.model.rest;

import com.example.model.order.Instrument;
import com.example.model.order.SubmittedOrder;

import java.util.Collection;

public sealed interface PortfolioResponse {
    record Portfolio(Collection<Element> portfolio,
                     Collection<SubmittedOrder.Buy> toBuy,
                     Collection<SubmittedOrder.Sell> toSell,
                     Long cash) implements PortfolioResponse {
        public record Element(Instrument instrument, long qty) {
        }
    }

    record Other(RestResponse restResponse) implements PortfolioResponse {
    }

}
