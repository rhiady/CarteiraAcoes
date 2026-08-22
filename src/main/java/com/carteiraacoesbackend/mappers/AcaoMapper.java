package com.carteiraacoesbackend.mappers;

import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.domains.Acao;
import com.carteiraacoesbackend.dto.AcaoResponse;

@Component
public class AcaoMapper {

    public AcaoResponse toResponse(Acao acao) {
        return new AcaoResponse(acao.getId(), acao.getTicker(), acao.getNomeEmpresa(), acao.getMercado(),
                acao.getMoeda(), acao.getCotacaoAtual(), acao.getDataHoraCotacao(), acao.getCreatedAt(),
                acao.getUpdatedAt());
    }
}
