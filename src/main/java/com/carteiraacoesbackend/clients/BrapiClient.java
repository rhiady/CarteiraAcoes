package com.carteiraacoesbackend.clients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "brapi", url = "${BRAPI_URL:https://brapi.dev/api}")
public interface BrapiClient {

    @GetMapping("/quote/{ticker}")
    Map<String, Object> consultar(@PathVariable String ticker, @RequestParam(required = false) String token);
}
