package com.carteiraacoesbackend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.carteiraacoesbackend.domain.Mercado;
import com.carteiraacoesbackend.domain.Moeda;

public record AcaoResponse(Long id, String ticker, String nomeEmpresa, Mercado mercado, Moeda moeda,
                           BigDecimal cotacaoAtual, OffsetDateTime dataHoraCotacao, OffsetDateTime createdAt,
                           OffsetDateTime updatedAt) {
}
