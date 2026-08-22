package com.carteiraacoesbackend.facades;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class CvmFacade {
    private final BrasilApiClient client;
    public CvmFacade(BrasilApiClient client) { this.client = client; }
    public Map<String, Object> consultarCorretora(String cnpj) {
        try {
            return client.listarCorretorasCvm().stream()
                    .filter(c -> cnpj.equals(limpar(c.get("cnpj"))))
                    .findFirst().orElseThrow(() -> ApiException.unprocessable("CORRETORA_NAO_REGISTRADA_CVM", "A corretora não possui registro ativo na CVM."));
        } catch (ApiException exception) { throw exception; }
        catch (Exception exception) { throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar o cadastro da CVM."); }
    }
    private String limpar(Object valor) { return valor == null ? "" : valor.toString().replaceAll("\\D", ""); }
}
