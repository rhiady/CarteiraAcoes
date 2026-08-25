package com.carteiraacoesbackend.integrations;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.clients.BrapiClient;
import com.carteiraacoesbackend.dto.integrations.BrapiQuoteResponse;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class BrapiCotacaoAdapter {

    private final BrapiClient client;
    private final String token;
    private final ExternalCallLogger externalCallLogger;

    public BrapiCotacaoAdapter(BrapiClient client, String token) {
        this(client, token, new ExternalCallLogger());
    }

    @Autowired
    public BrapiCotacaoAdapter(BrapiClient client, @Value("${BRAPI_TOKEN:}") String token,
            ExternalCallLogger externalCallLogger) {
        this.client = client;
        this.token = token;
        this.externalCallLogger = externalCallLogger;
    }

    public Cotacao consultar(String ticker) {
        BrapiQuoteResponse body = externalCallLogger.execute("brapi", "quote", ticker,
                () -> client.consultar(ticker, token.isBlank() ? null : token));
        if (body.results() == null || body.results().isEmpty() || body.results().getFirst().regularMarketPrice() == null) {
            throw ApiException.notFound("TICKER_NAO_ENCONTRADO", "Ticker não encontrado no provedor de mercado.");
        }
        BrapiQuoteResponse.Result result = body.results().getFirst();
        String nomeEmpresa = result.longName() == null || result.longName().isBlank() ? result.shortName() : result.longName();
        if (nomeEmpresa == null || nomeEmpresa.isBlank()) {
            throw ApiException.external(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "EXTERNAL_API_INVALID_RESPONSE", "O provedor não retornou o nome da empresa.");
        }
        return new Cotacao(result.regularMarketPrice(), OffsetDateTime.now(ZoneOffset.UTC), nomeEmpresa);
    }
}
