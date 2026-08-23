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

class CvmFacadeTest {

    @Test
    void acceptsBrokerOnlyWhenCnpjAndCvmCodeArePresent() {
        CvmFacade facade = new CvmFacade(new FakeBrasilApiClient(List.of(
                new BrasilApiCvmBrokerResponse("12.345.678/0001-90", "12345"))));

        assertEquals("12345", facade.consultarCorretora("12345678000190").codigoCvm());
    }

    @Test
    void rejectsBrokerMissingFromCvmOrWithoutCvmCode() {
        assertNotRegistered(List.of());
        assertNotRegistered(List.of(new BrasilApiCvmBrokerResponse("12345678000190", " ")));
    }

    private void assertNotRegistered(List<BrasilApiCvmBrokerResponse> records) {
        CvmFacade facade = new CvmFacade(new FakeBrasilApiClient(records));
        ApiException exception = assertThrows(ApiException.class,
                () -> facade.consultarCorretora("12345678000190"));
        assertEquals("CORRETORA_NAO_REGISTRADA_CVM", exception.getCode());
        assertEquals(422, exception.getStatus().value());
    }

    private record FakeBrasilApiClient(List<BrasilApiCvmBrokerResponse> records) implements BrasilApiClient {
        @Override public BrasilApiCnpjResponse consultarCnpj(String cnpj) { throw new UnsupportedOperationException(); }
        @Override public BrasilApiCepResponse consultarCep(String cep) { throw new UnsupportedOperationException(); }
        @Override public List<BrasilApiCvmBrokerResponse> listarCorretorasCvm() { return records; }
    }
}
