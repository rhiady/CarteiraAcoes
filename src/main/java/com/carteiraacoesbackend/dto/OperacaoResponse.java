package com.carteiraacoesbackend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.carteiraacoesbackend.domain.TipoOperacao;

public record OperacaoResponse(Long id, Long carteiraId, Long acaoId, TipoOperacao tipo, BigDecimal quantidade,
                               BigDecimal precoUnitario, BigDecimal valorBruto, BigDecimal corretagem,
                               BigDecimal impostos, BigDecimal valorAdicional, BigDecimal valorLiquido,
                               OffsetDateTime dataHora, OffsetDateTime createdAt) {
}
