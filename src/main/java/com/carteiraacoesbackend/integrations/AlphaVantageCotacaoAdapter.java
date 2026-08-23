package com.carteiraacoesbackend.integrations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.clients.AlphaVantageClient;
import com.carteiraacoesbackend.dto.integrations.AlphaVantageQuoteResponse;
import com.carteiraacoesbackend.dto.integrations.AlphaVantageCompanyResponse;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class AlphaVantageCotacaoAdapter {

    private final AlphaVantageClient client;
    private final String apiKey;

    public AlphaVantageCotacaoAdapter(AlphaVantageClient client, @Value("${ALPHA_VANTAGE_API_KEY:}") String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    public Cotacao consultar(String ticker) {
        AlphaVantageQuoteResponse body = client.consultar("GLOBAL_QUOTE", ticker, apiKey);
        if (body.globalQuote() == null || body.globalQuote().price() == null) {
            throw ApiException.notFound("TICKER_NAO_ENCONTRADO", "Ticker não encontrado no provedor de mercado.");
        }
        AlphaVantageCompanyResponse empresa = client.consultarEmpresa("OVERVIEW", ticker, apiKey);
        if (empresa.name() == null || empresa.name().isBlank()) {
            throw ApiException.external(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "EXTERNAL_API_INVALID_RESPONSE", "O provedor não retornou o nome da empresa.");
        }
        return new Cotacao(new BigDecimal(body.globalQuote().price()), OffsetDateTime.now(ZoneOffset.UTC), empresa.name());
    }
}
