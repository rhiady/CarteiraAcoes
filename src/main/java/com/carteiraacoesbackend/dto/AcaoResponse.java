package com.carteiraacoesbackend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.carteiraacoesbackend.domains.enums.Mercado;
import com.carteiraacoesbackend.domains.enums.Moeda;

public record AcaoResponse(Long id, String ticker, String nomeEmpresa, Mercado mercado, Moeda moeda,
                           BigDecimal cotacaoAtual, OffsetDateTime dataHoraCotacao, OffsetDateTime createdAt,
                           OffsetDateTime updatedAt) {
}
