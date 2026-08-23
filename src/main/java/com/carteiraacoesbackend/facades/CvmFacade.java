package com.carteiraacoesbackend.facades;

import org.springframework.stereotype.Component;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCvmBrokerResponse;

@Component
public class CvmFacade {
    private final BrasilApiClient client;
    public CvmFacade(BrasilApiClient client) { this.client = client; }
    public BrasilApiCvmBrokerResponse consultarCorretora(String cnpj) {
        String normalizado = limpar(cnpj);
        if (normalizado.length() != 14) throw ApiException.unprocessable("CNPJ_INVALIDO", "CNPJ deve conter 14 dígitos.");
        try {
            return client.listarCorretorasCvm().stream()
                    .filter(c -> normalizado.equals(limpar(c.cnpj())))
                    .findFirst().orElseThrow(() -> ApiException.unprocessable("CORRETORA_NAO_REGISTRADA_CVM", "A corretora não possui registro ativo na CVM."));
        } catch (ApiException exception) { throw exception; }
        catch (Exception exception) { throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar o cadastro da CVM."); }
    }
    private String limpar(Object valor) { return valor == null ? "" : valor.toString().replaceAll("\\D", ""); }
}
