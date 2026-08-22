package com.carteiraacoesbackend.dto;

import jakarta.validation.constraints.NotBlank;

public record CorretoraRequest(@NotBlank String cnpj, @NotBlank String cep, @NotBlank String numero,
                               String complemento) {
}
