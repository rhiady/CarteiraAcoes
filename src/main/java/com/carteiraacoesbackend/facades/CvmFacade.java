package com.carteiraacoesbackend.facades;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCvmBrokerResponse;
import com.carteiraacoesbackend.integrations.ExternalCallLogger;

@Component
public class CvmFacade {
    private final BrasilApiClient client;
    private final ExternalCallLogger externalCallLogger;
    public CvmFacade(BrasilApiClient client) {
        this(client, new ExternalCallLogger());
    }
    @Autowired
    public CvmFacade(BrasilApiClient client, ExternalCallLogger externalCallLogger) {
        this.client = client;
        this.externalCallLogger = externalCallLogger;
    }
    public BrasilApiCvmBrokerResponse consultarCorretora(String cnpj) {
        String normalizado = limpar(cnpj);
        if (normalizado.length() != 14) throw ApiException.unprocessable("CNPJ_INVALIDO", "CNPJ deve conter 14 dígitos.");
        try {
            return externalCallLogger.execute("brasil-api", "cvm-brokers", normalizado, client::listarCorretorasCvm).stream()
                    .filter(c -> normalizado.equals(limpar(c.cnpj())) && c.codigoCvm() != null
                            && !c.codigoCvm().isBlank())
                    .findFirst().orElseThrow(() -> ApiException.unprocessable("CORRETORA_NAO_REGISTRADA_CVM", "A corretora não possui registro ativo na CVM."));
        } catch (ApiException exception) { throw exception; }
        catch (Exception exception) { throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar o cadastro da CVM."); }
    }
    private String limpar(Object valor) { return valor == null ? "" : valor.toString().replaceAll("\\D", ""); }
}
