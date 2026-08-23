package com.carteiraacoesbackend.integrations;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.carteiraacoesbackend.domains.enums.Mercado;
import com.carteiraacoesbackend.exceptions.ApiException;

@Component
@Primary
public class DefaultCotacaoAdapter implements CotacaoAdapter {

    private final BrapiCotacaoAdapter brapi;
    private final AlphaVantageCotacaoAdapter alphaVantage;

    public DefaultCotacaoAdapter(BrapiCotacaoAdapter brapi, AlphaVantageCotacaoAdapter alphaVantage) {
        this.brapi = brapi;
        this.alphaVantage = alphaVantage;
    }

    @Override
    public Cotacao consultar(String ticker, Mercado mercado) {
        try {
            if (mercado == Mercado.BRASIL) {
                return brapi.consultar(ticker);
            }
            return alphaVantage.consultar(ticker);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw ApiException.external(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "EXTERNAL_API_UNAVAILABLE", "Não foi possível consultar a cotação externa.");
        }
    }
}
