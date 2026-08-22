package com.carteiraacoesbackend.dto;

import com.carteiraacoesbackend.domain.Mercado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcaoRequest(@NotBlank String ticker, @NotBlank String nomeEmpresa, @NotNull Mercado mercado) {
}
