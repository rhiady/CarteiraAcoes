package com.carteiraacoesbackend.facades;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class CnpjFacade {
    private final BrasilApiClient client;
    public CnpjFacade(BrasilApiClient client) { this.client = client; }
    public Map<String, Object> consultar(String cnpj) {
        try { return client.consultarCnpj(cnpj); }
        catch (Exception e) { throw ApiException.external(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "CNPJ_NAO_ENCONTRADO", "CNPJ não encontrado ou inválido."); }
    }
}
