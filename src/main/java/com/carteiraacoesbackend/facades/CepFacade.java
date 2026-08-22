package com.carteiraacoesbackend.facades;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class CepFacade {
    private final BrasilApiClient client;
    public CepFacade(BrasilApiClient client) { this.client = client; }
    public Map<String, Object> consultar(String cep) {
        try { return client.consultarCep(cep); }
        catch (Exception e) { throw ApiException.external(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "CEP_NAO_ENCONTRADO", "CEP não encontrado ou inválido."); }
    }
}
