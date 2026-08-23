package com.carteiraacoesbackend.dto.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlphaVantageCompanyResponse(@JsonProperty("Name") String name) {
}
