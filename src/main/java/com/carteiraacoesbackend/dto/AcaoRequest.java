package com.carteiraacoesbackend.dto;

import com.carteiraacoesbackend.domains.enums.Mercado;
import jakarta.validation.constraints.NotNull;

public record AcaoRequest(@jakarta.validation.constraints.NotBlank String ticker, @NotNull Mercado mercado) {
}
