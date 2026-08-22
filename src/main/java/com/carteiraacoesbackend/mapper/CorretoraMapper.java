package com.carteiraacoesbackend.mapper;

import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.domain.Corretora;
import com.carteiraacoesbackend.dto.CorretoraResponse;

@Component
public class CorretoraMapper {

    public CorretoraResponse toResponse(Corretora corretora) {
        return new CorretoraResponse(corretora.getId(), corretora.getCnpj(), corretora.getRazaoSocial(),
                corretora.getNomeFantasia(), corretora.getEmail(), corretora.getTelefone(), corretora.getCep(),
                corretora.getLogradouro(), corretora.getNumero(), corretora.getComplemento(), corretora.getBairro(),
                corretora.getCidade(), corretora.getUf(), corretora.getSituacaoCadastral(), corretora.getRegistroCvm(),
                corretora.getDataValidacaoCvm(), corretora.getCreatedAt(), corretora.getUpdatedAt());
    }
}
