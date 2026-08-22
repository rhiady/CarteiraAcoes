package com.carteiraacoesbackend.clients;

import java.util.Map;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "brasilApi", url = "${BRASIL_API_URL:https://brasilapi.com.br/api}")
public interface BrasilApiClient {
    @GetMapping("/cnpj/v1/{cnpj}") Map<String, Object> consultarCnpj(@PathVariable String cnpj);
    @GetMapping("/cep/v2/{cep}") Map<String, Object> consultarCep(@PathVariable String cep);
    @GetMapping("/cvm/corretoras/v1") List<Map<String, Object>> listarCorretorasCvm();
}
