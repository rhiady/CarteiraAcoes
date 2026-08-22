package com.carteiraacoesbackend.clients;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import com.carteiraacoesbackend.exceptions.ApiException;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> ApiException.notFound("EXTERNAL_RESOURCE_NOT_FOUND", "Recurso não encontrado no provedor externo.");
            case 401, 403 -> ApiException.external(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_AUTHENTICATION", "Falha de autenticação no provedor externo.");
            case 429 -> ApiException.external(HttpStatus.TOO_MANY_REQUESTS, "EXTERNAL_API_RATE_LIMIT", "Limite de uso do provedor externo excedido.");
            case 500, 502, 503, 504 -> ApiException.external(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "Provedor externo indisponível.");
            default -> ApiException.external(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR", "Falha na comunicação com o provedor externo.");
        };
    }
}
