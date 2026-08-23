package com.carteiraacoesbackend.facades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCepResponse;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCnpjResponse;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCvmBrokerResponse;
import com.carteiraacoesbackend.exceptions.ApiException;

class CnpjFacadeTest {

    @Test
    void normalizesCnpjBeforeCallingClient() {
        CnpjFacade facade = new CnpjFacade(new FakeBrasilApiClient());
        assertEquals("Empresa", facade.consultar("12.345.678/0001-90").razaoSocial());
    }

    @Test
    void classifiesClientTimeoutAsUnavailable() {
        CnpjFacade facade = new CnpjFacade(new FakeBrasilApiClient() {
            @Override public BrasilApiCnpjResponse consultarCnpj(String cnpj) { throw new RuntimeException("timeout"); }
        });
        ApiException error = assertThrows(ApiException.class, () -> facade.consultar("12345678000190"));
        assertEquals("EXTERNAL_API_UNAVAILABLE", error.getCode());
        assertEquals(503, error.getStatus().value());
    }

    private static class FakeBrasilApiClient implements BrasilApiClient {
        @Override public BrasilApiCnpjResponse consultarCnpj(String cnpj) {
            if (!"12345678000190".equals(cnpj)) throw new AssertionError("CNPJ não normalizado");
            return new BrasilApiCnpjResponse("Empresa", "Empresa", "email@test.com", "11999999999", "ATIVA",
                    "01001000", "Rua", "100", null, "Centro", "São Paulo", "SP");
        }
        @Override public BrasilApiCepResponse consultarCep(String cep) { throw new UnsupportedOperationException(); }
        @Override public List<BrasilApiCvmBrokerResponse> listarCorretorasCvm() { throw new UnsupportedOperationException(); }
    }
}
