package com.carteiraacoesbackend.mapper;

import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.domain.CarteiraAcao;
import com.carteiraacoesbackend.dto.CarteiraAcaoResponse;

@Component
public class CarteiraAcaoMapper {

    public CarteiraAcaoResponse toResponse(CarteiraAcao posicao) {
        return new CarteiraAcaoResponse(posicao.getId(), posicao.getCarteira().getId(), posicao.getAcao().getId(),
                posicao.getAcao().getTicker(), posicao.getQuantidade(), posicao.getVersion());
    }
}
