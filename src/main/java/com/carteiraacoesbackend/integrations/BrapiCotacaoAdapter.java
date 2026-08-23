package com.carteiraacoesbackend.integrations;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.clients.BrapiClient;
import com.carteiraacoesbackend.dto.integrations.BrapiQuoteResponse;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class BrapiCotacaoAdapter {

    private final BrapiClient client;
    private final String token;

    public BrapiCotacaoAdapter(BrapiClient client, @Value("${BRAPI_TOKEN:}") String token) {
        this.client = client;
        this.token = token;
    }

    public Cotacao consultar(String ticker) {
        BrapiQuoteResponse body = client.consultar(ticker, token.isBlank() ? null : token);
        if (body.results() == null || body.results().isEmpty() || body.results().getFirst().regularMarketPrice() == null) {
            throw ApiException.notFound("TICKER_NAO_ENCONTRADO", "Ticker não encontrado no provedor de mercado.");
        }
        return new Cotacao(body.results().getFirst().regularMarketPrice(), OffsetDateTime.now(ZoneOffset.UTC));
    }
}
