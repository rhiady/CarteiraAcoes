package com.carteiraacoesbackend.integrations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.carteiraacoesbackend.clients.AlphaVantageClient;
import com.carteiraacoesbackend.clients.BrapiClient;
import com.carteiraacoesbackend.dto.integrations.AlphaVantageCompanyResponse;
import com.carteiraacoesbackend.dto.integrations.AlphaVantageQuoteResponse;
import com.carteiraacoesbackend.dto.integrations.BrapiQuoteResponse;

class CotacaoAdapterDetailsTest {

    @Test
    void importsBrazilianCompanyNameFromBrapiQuote() {
        BrapiClient client = (ticker, token) -> new BrapiQuoteResponse(List.of(
                new BrapiQuoteResponse.Result(new BigDecimal("38.50"), "Petróleo Brasileiro S.A.", "PETROBRAS PN")));

        Cotacao quote = new BrapiCotacaoAdapter(client, "").consultar("PETR4");

        assertEquals("Petróleo Brasileiro S.A.", quote.nomeEmpresa());
    }

    @Test
    void importsAmericanCompanyNameFromAlphaVantageOverview() {
        AlphaVantageClient client = new AlphaVantageClient() {
            @Override public AlphaVantageQuoteResponse consultar(String function, String ticker, String apiKey) {
                return new AlphaVantageQuoteResponse(new AlphaVantageQuoteResponse.Quote("123.45"));
            }
            @Override public AlphaVantageCompanyResponse consultarEmpresa(String function, String ticker, String apiKey) {
                return new AlphaVantageCompanyResponse("International Business Machines Corporation");
            }
        };

        Cotacao quote = new AlphaVantageCotacaoAdapter(client, "key").consultar("IBM");

        assertEquals("International Business Machines Corporation", quote.nomeEmpresa());
    }
}
