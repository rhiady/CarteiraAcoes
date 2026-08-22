package com.carteiraacoesbackend.integrations;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.clients.AlphaVantageClient;
import com.carteiraacoesbackend.clients.BrapiClient;
import com.carteiraacoesbackend.domains.enums.Mercado;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
public class DefaultCotacaoAdapter implements CotacaoAdapter {

    private final BrapiClient brapiClient;
    private final AlphaVantageClient alphaVantageClient;
    private final String brapiToken;
    private final String alphaVantageApiKey;

    public DefaultCotacaoAdapter(BrapiClient brapiClient, AlphaVantageClient alphaVantageClient,
                                 @Value("${BRAPI_TOKEN:}") String brapiToken,
                                 @Value("${ALPHA_VANTAGE_API_KEY:}") String alphaVantageApiKey) {
        this.brapiClient = brapiClient;
        this.alphaVantageClient = alphaVantageClient;
        this.brapiToken = brapiToken;
        this.alphaVantageApiKey = alphaVantageApiKey;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Cotacao consultar(String ticker, Mercado mercado) {
        try {
            if (mercado == Mercado.BRASIL) {
                Map<String, Object> body = brapiClient.consultar(ticker, brapiToken.isBlank() ? null : brapiToken);
                List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
                if (results == null || results.isEmpty() || results.getFirst().get("regularMarketPrice") == null) {
                    throw ApiException.notFound("TICKER_NAO_ENCONTRADO", "Ticker não encontrado no provedor de mercado.");
                }
                Map<String, Object> quote = results.getFirst();
                return new Cotacao(decimal(quote.get("regularMarketPrice")), OffsetDateTime.now(ZoneOffset.UTC));
            }
            Map<String, Object> body = alphaVantageClient.consultar("GLOBAL_QUOTE", ticker, alphaVantageApiKey);
            Map<String, Object> quote = (Map<String, Object>) body.get("Global Quote");
            if (quote == null || quote.get("05. price") == null) {
                throw ApiException.notFound("TICKER_NAO_ENCONTRADO", "Ticker não encontrado no provedor de mercado.");
            }
            return new Cotacao(decimal(quote.get("05. price")), OffsetDateTime.now(ZoneOffset.UTC));
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar a cotação externa.");
        }
    }

    private BigDecimal decimal(Object value) {
        return new BigDecimal(value.toString());
    }
}
