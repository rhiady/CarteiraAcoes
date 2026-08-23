package com.carteiraacoesbackend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.carteiraacoesbackend.clients.BrasilApiClient;
import com.carteiraacoesbackend.dto.CorretoraRequest;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCepResponse;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCnpjResponse;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCvmBrokerResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.facades.CnpjFacade;
import com.carteiraacoesbackend.facades.CvmFacade;
import com.carteiraacoesbackend.mappers.CorretoraMapper;
import com.carteiraacoesbackend.repositories.CorretoraRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CorretoraServiceCvmTest {

    @Autowired
    private CorretoraRepository repository;

    @Test
    void doesNotPersistBrokerWithoutValidCvmRegistration() {
        BrasilApiClient client = new FakeBrasilApiClient(List.of());
        CorretoraService service = new CorretoraService(repository, new CnpjFacade(client), new CvmFacade(client),
                new CorretoraMapper());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.criar(new CorretoraRequest("12.345.678/0001-90")));

        assertEquals("CORRETORA_NAO_REGISTRADA_CVM", exception.getCode());
        assertEquals(0, repository.count());
    }

    @Test
    void importsBrokerAddressFromTheCnpjResponse() {
        BrasilApiClient client = new FakeBrasilApiClient(List.of(
                new BrasilApiCvmBrokerResponse("12345678000190", "1234")));
        CorretoraService service = new CorretoraService(repository, new CnpjFacade(client), new CvmFacade(client),
                new CorretoraMapper());

        var response = service.criar(new CorretoraRequest("12.345.678/0001-90"));

        assertEquals("01001000", response.cep());
        assertEquals("Rua", response.logradouro());
        assertEquals("100", response.numero());
        assertEquals("São Paulo", response.cidade());
    }

    private static final class FakeBrasilApiClient implements BrasilApiClient {
        private final List<BrasilApiCvmBrokerResponse> registrosCvm;

        private FakeBrasilApiClient(List<BrasilApiCvmBrokerResponse> registrosCvm) {
            this.registrosCvm = registrosCvm;
        }

        @Override public BrasilApiCnpjResponse consultarCnpj(String cnpj) {
            return new BrasilApiCnpjResponse("Empresa", "Empresa", "email@test.com", "11999999999", "ATIVA",
                    "01001000", "Rua", "100", null, "Centro", "São Paulo", "SP");
        }
        @Override public BrasilApiCepResponse consultarCep(String cep) {
            return new BrasilApiCepResponse("Rua", "Centro", "São Paulo", "SP");
        }
        @Override public List<BrasilApiCvmBrokerResponse> listarCorretorasCvm() { return registrosCvm; }
    }
}
