package com.carteiraacoesbackend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.carteiraacoesbackend.domains.Acao;
import com.carteiraacoesbackend.domains.Carteira;
import com.carteiraacoesbackend.domains.CarteiraAcao;
import com.carteiraacoesbackend.domains.Usuario;
import com.carteiraacoesbackend.domains.enums.Mercado;
import com.carteiraacoesbackend.domains.enums.Moeda;
import com.carteiraacoesbackend.dto.CompraRequest;
import com.carteiraacoesbackend.dto.VendaRequest;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.integrations.Cotacao;
import com.carteiraacoesbackend.integrations.CotacaoAdapter;
import com.carteiraacoesbackend.repositories.AcaoRepository;
import com.carteiraacoesbackend.repositories.CarteiraAcaoRepository;
import com.carteiraacoesbackend.repositories.CarteiraRepository;
import com.carteiraacoesbackend.repositories.OperacaoRepository;
import com.carteiraacoesbackend.repositories.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
class OperacaoServiceIntegrationTest {
    @Autowired OperacaoService service;
    @Autowired UsuarioRepository usuarios;
    @Autowired CarteiraRepository carteiras;
    @Autowired AcaoRepository acoes;
    @Autowired CarteiraAcaoRepository posicoes;
    @Autowired OperacaoRepository operacoes;
    @MockitoBean CotacaoAdapter cotacaoAdapter;
    private Carteira carteira; private Acao acao;

    @BeforeEach
    void setup() {
        operacoes.deleteAll(); posicoes.deleteAll(); carteiras.deleteAll(); acoes.deleteAll(); usuarios.deleteAll();
        Usuario usuario = new Usuario(); usuario.setNome("Ana"); usuario.setEmail("ana" + System.nanoTime() + "@test.com"); usuario.setSenha("senha"); usuarios.save(usuario);
        carteira = new Carteira(); carteira.setNome("Carteira"); carteira.setUsuario(usuario); carteira = carteiras.save(carteira);
        acao = new Acao(); acao.setTicker("TEST3"); acao.setNomeEmpresa("Teste"); acao.setMercado(Mercado.BRASIL); acao.setMoeda(Moeda.BRL); acao = acoes.save(acao);
        when(cotacaoAdapter.consultar(any(), any())).thenReturn(new Cotacao(new BigDecimal("10.00"), java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)));
    }

    @Test
    void sellsEntirePositionAndKeepsZeroPosition() {
        service.comprar(new CompraRequest(carteira.getId(), acao.getId(), new BigDecimal("2"), new BigDecimal("10"), null, null, null));
        service.vender(new VendaRequest(carteira.getId(), acao.getId(), new BigDecimal("2"), null, null, null));
        CarteiraAcao posicao = posicoes.findByCarteiraIdAndAcaoId(carteira.getId(), acao.getId()).orElseThrow();
        assertThat(posicao.getQuantidade()).isEqualByComparingTo("0");
        assertThat(posicao.getVersion()).isGreaterThan(0L);
        assertThat(operacoes.findByCarteiraIdOrderByDataHoraDesc(carteira.getId(), org.springframework.data.domain.Pageable.unpaged())).hasSize(2);
    }

    @Test
    void rejectsSaleAbovePositionWithoutCreatingOperation() {
        assertThatThrownBy(() -> service.vender(new VendaRequest(carteira.getId(), acao.getId(), BigDecimal.ONE, null, null, null)))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("QUANTIDADE_INSUFICIENTE");
        assertThat(operacoes.count()).isZero();
    }

    @Test
    void rollsBackPurchaseWhenQuoteFails() {
        when(cotacaoAdapter.consultar(any(), any())).thenThrow(new ApiException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_UNAVAILABLE", "indisponível"));
        assertThatThrownBy(() -> service.comprar(new CompraRequest(carteira.getId(), acao.getId(), BigDecimal.ONE, null, null, null, null))).isInstanceOf(ApiException.class);
        assertThat(operacoes.count()).isZero(); assertThat(posicoes.count()).isZero();
    }
}
