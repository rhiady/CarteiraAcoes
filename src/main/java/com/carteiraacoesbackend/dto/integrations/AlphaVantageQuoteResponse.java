package com.carteiraacoesbackend.dto.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlphaVantageQuoteResponse(@JsonProperty("Global Quote") Quote globalQuote) {
    public record Quote(@JsonProperty("05. price") String price) { }
}
