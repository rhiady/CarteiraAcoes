package com.carteiraacoesbackend.mappers;

import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.domains.Carteira;
import com.carteiraacoesbackend.dto.CarteiraResponse;

@Component
public class CarteiraMapper {

    public CarteiraResponse toResponse(Carteira carteira) {
        return new CarteiraResponse(carteira.getId(), carteira.getNome(), carteira.getUsuario().getId(),
                carteira.getCreatedAt(), carteira.getUpdatedAt());
    }
}
