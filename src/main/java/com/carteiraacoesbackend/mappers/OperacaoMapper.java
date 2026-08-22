package com.carteiraacoesbackend.mappers;

import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.domains.Operacao;
import com.carteiraacoesbackend.dto.OperacaoResponse;

@Component
public class OperacaoMapper {

    public OperacaoResponse toResponse(Operacao operacao) {
        return new OperacaoResponse(operacao.getId(), operacao.getCarteira().getId(), operacao.getAcao().getId(),
                operacao.getTipo(), operacao.getQuantidade(), operacao.getPrecoUnitario(), operacao.getValorBruto(),
                operacao.getCorretagem(), operacao.getImpostos(), operacao.getValorAdicional(),
                operacao.getValorLiquido(), operacao.getDataHora(), operacao.getCreatedAt());
    }
}
