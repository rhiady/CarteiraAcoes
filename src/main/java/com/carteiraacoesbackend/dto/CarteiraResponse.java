package com.carteiraacoesbackend.dto;

import java.time.OffsetDateTime;

public record CarteiraResponse(Long id, String nome, Long usuarioId, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
