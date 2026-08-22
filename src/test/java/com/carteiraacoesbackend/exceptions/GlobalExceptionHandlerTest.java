package com.carteiraacoesbackend.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import com.carteiraacoesbackend.dto.ErroResponse;

class GlobalExceptionHandlerTest {

    @Test
    void returnsStandardErrorBodyForBusinessErrors() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/operacoes/vendas");
        var response = new GlobalExceptionHandler().handleApi(
                ApiException.unprocessable("QUANTIDADE_INSUFICIENTE", "Quantidade indisponível."), request);

        ErroResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(body).isNotNull();
        assertThat(body.error()).isEqualTo("QUANTIDADE_INSUFICIENTE");
        assertThat(body.path()).isEqualTo("/operacoes/vendas");
        assertThat(body.timestamp()).isNotNull();
    }
}
