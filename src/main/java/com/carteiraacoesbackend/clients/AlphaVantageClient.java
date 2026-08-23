package com.carteiraacoesbackend.clients;

import com.carteiraacoesbackend.dto.integrations.AlphaVantageQuoteResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "alphaVantage", url = "${ALPHA_VANTAGE_URL:https://www.alphavantage.co}")
public interface AlphaVantageClient {

    @GetMapping("/query")
    AlphaVantageQuoteResponse consultar(@RequestParam("function") String function, @RequestParam("symbol") String ticker,
                                  @RequestParam("apikey") String apiKey);
}
