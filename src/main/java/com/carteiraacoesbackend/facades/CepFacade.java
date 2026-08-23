package com.carteiraacoesbackend.facades;

import org.springframework.stereotype.Component;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCepResponse;

@Component
public class CepFacade {
    private final BrasilApiClient client;
    public CepFacade(BrasilApiClient client) { this.client = client; }
    public BrasilApiCepResponse consultar(String cep) {
        String normalizado = somenteDigitos(cep);
        if (normalizado.length() != 8) throw ApiException.unprocessable("CEP_INVALIDO", "CEP deve conter 8 dígitos.");
        try { return client.consultarCep(normalizado); }
        catch (ApiException exception) { throw exception; }
        catch (Exception e) { throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar o CEP."); }
    }
    private String somenteDigitos(String valor) { return valor == null ? "" : valor.replaceAll("\\D", ""); }
}
