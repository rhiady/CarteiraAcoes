package com.carteiraacoesbackend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CompraRequest(@NotNull Long carteiraId, @NotNull Long acaoId,
                            @NotNull @DecimalMin("0.0001") BigDecimal quantidade,
                            @DecimalMin("0.0001") BigDecimal precoUnitario,
                            @DecimalMin("0") BigDecimal corretagem, @DecimalMin("0") BigDecimal impostos,
                            @DecimalMin("0") BigDecimal valorAdicional) {
}
