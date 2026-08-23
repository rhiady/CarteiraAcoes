package com.carteiraacoesbackend.facades;

import org.springframework.stereotype.Component;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCnpjResponse;

@Component
public class CnpjFacade {
    private final BrasilApiClient client;
    public CnpjFacade(BrasilApiClient client) { this.client = client; }
    public BrasilApiCnpjResponse consultar(String cnpj) {
        String normalizado = somenteDigitos(cnpj);
        if (normalizado.length() != 14) throw ApiException.unprocessable("CNPJ_INVALIDO", "CNPJ deve conter 14 dígitos.");
        try { return client.consultarCnpj(normalizado); }
        catch (ApiException exception) { throw exception; }
        catch (Exception e) { throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar o CNPJ."); }
    }
    private String somenteDigitos(String valor) { return valor == null ? "" : valor.replaceAll("\\D", ""); }
}
