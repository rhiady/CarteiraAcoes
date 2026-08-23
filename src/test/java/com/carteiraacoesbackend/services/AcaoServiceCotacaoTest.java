package com.carteiraacoesbackend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.carteiraacoesbackend.domains.enums.Mercado;
import com.carteiraacoesbackend.dto.AcaoRequest;
import com.carteiraacoesbackend.dto.AcaoResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.integrations.Cotacao;
import com.carteiraacoesbackend.integrations.CotacaoAdapter;
import com.carteiraacoesbackend.mappers.AcaoMapper;
import com.carteiraacoesbackend.repositories.AcaoRepository;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AcaoServiceCotacaoTest {

    @Autowired
    private AcaoRepository repository;

    private StubCotacaoAdapter cotacaoAdapter;
    private AcaoService service;

    @BeforeEach
    void setUp() {
        cotacaoAdapter = new StubCotacaoAdapter();
        service = new AcaoService(repository, cotacaoAdapter, new AcaoMapper());
    }

    @Test
    void updatesQuoteWithDecimalPriceAndUtcTimestamp() {
        cotacaoAdapter.cotacao = cotacao("10.0000", "Petrobras S.A.");
        AcaoResponse acao = service.criar(new AcaoRequest("PETR4", Mercado.BRASIL));
        cotacaoAdapter.cotacao = new Cotacao(new BigDecimal("15.1234"), OffsetDateTime.parse("2026-08-23T10:00:00-03:00"));

        AcaoResponse updated = service.atualizarCotacao(acao.id());

        assertEquals(new BigDecimal("15.1234"), updated.cotacaoAtual());
        assertEquals(OffsetDateTime.parse("2026-08-23T13:00:00Z"), updated.dataHoraCotacao());
        assertEquals("Petrobras S.A.", updated.nomeEmpresa());
    }

    @Test
    void preservesStoredQuoteForEveryExternalFailureCategory() {
        verificarFalha("VALE3", ApiException.notFound("TICKER_NAO_ENCONTRADO", "não encontrado"), 404,
                "TICKER_NAO_ENCONTRADO");
        verificarFalha("ITUB4", ApiException.external(org.springframework.http.HttpStatus.BAD_GATEWAY,
                "EXTERNAL_API_AUTHENTICATION", "sem credencial"), 502, "EXTERNAL_API_AUTHENTICATION");
        verificarFalha("BBAS3", ApiException.external(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                "EXTERNAL_API_RATE_LIMIT", "limite"), 429, "EXTERNAL_API_RATE_LIMIT");
        verificarFalha("ABEV3", ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "EXTERNAL_API_UNAVAILABLE", "timeout"), 503, "EXTERNAL_API_UNAVAILABLE");
    }

    @Test
    void rejectsUnknownStockBeforeCallingTheProvider() {
        ApiException exception = assertThrows(ApiException.class, () -> service.atualizarCotacao(99999L));
        assertEquals("ACAO_NAO_ENCONTRADA", exception.getCode());
    }

    private void verificarFalha(String ticker, ApiException failure, int expectedStatus, String expectedCode) {
        cotacaoAdapter.cotacao = cotacao("10.0000", "Empresa importada");
        AcaoResponse acao = service.criar(new AcaoRequest(ticker, Mercado.BRASIL));
        cotacaoAdapter.failure = failure;

        ApiException exception = assertThrows(ApiException.class, () -> service.atualizarCotacao(acao.id()));

        assertEquals(expectedStatus, exception.getStatus().value());
        assertEquals(expectedCode, exception.getCode());
        assertEquals(new BigDecimal("10.0000"), service.buscarPorId(acao.id()).cotacaoAtual());
        cotacaoAdapter.failure = null;
    }

    private Cotacao cotacao(String preco, String nomeEmpresa) {
        return new Cotacao(new BigDecimal(preco), OffsetDateTime.now(ZoneOffset.UTC), nomeEmpresa);
    }

    private static final class StubCotacaoAdapter implements CotacaoAdapter {
        private Cotacao cotacao;
        private RuntimeException failure;

        @Override
        public Cotacao consultar(String ticker, Mercado mercado) {
            if (failure != null) {
                throw failure;
            }
            return cotacao;
        }
    }
}
