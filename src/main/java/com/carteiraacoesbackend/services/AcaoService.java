package com.carteiraacoesbackend.services;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carteiraacoesbackend.domains.Acao;
import com.carteiraacoesbackend.domains.enums.Moeda;
import com.carteiraacoesbackend.dto.AcaoResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.integrations.Cotacao;
import com.carteiraacoesbackend.integrations.CotacaoAdapter;
import com.carteiraacoesbackend.mappers.AcaoMapper;
import com.carteiraacoesbackend.repositories.AcaoRepository;

@Service
@Transactional(readOnly = true)
public class AcaoService {

    private final AcaoRepository repository;
    private final CotacaoAdapter cotacaoAdapter;
    private final AcaoMapper mapper;

    public AcaoService(AcaoRepository repository, CotacaoAdapter cotacaoAdapter, AcaoMapper mapper) {
        this.repository = repository;
        this.cotacaoAdapter = cotacaoAdapter;
        this.mapper = mapper;
    }

    @Transactional
    public Acao resolverOuCriar(String ticker, com.carteiraacoesbackend.domains.enums.Mercado mercado) {
        if (ticker == null || ticker.isBlank() || mercado == null) {
            throw ApiException.unprocessable("IDENTIFICACAO_ACAO_INVALIDA", "Informe acaoId ou ticker e mercado.");
        }
        String tickerNormalizado = ticker.trim().toUpperCase(Locale.ROOT);
        return repository.findByTickerIgnoreCase(tickerNormalizado)
                .orElseGet(() -> criarEntidade(tickerNormalizado, mercado));
    }

    private Acao criarEntidade(String tickerInformado, com.carteiraacoesbackend.domains.enums.Mercado mercado) {
        String ticker = tickerInformado.trim().toUpperCase(Locale.ROOT);
        if (repository.existsByTickerIgnoreCase(ticker)) {
            throw ApiException.conflict("TICKER_DUPLICADO", "Já existe uma ação com este ticker.");
        }
        Cotacao cotacao = cotacaoAdapter.consultar(ticker, mercado);
        validarDadosCadastro(cotacao);
        Acao acao = new Acao();
        acao.setTicker(ticker);
        acao.setNomeEmpresa(cotacao.nomeEmpresa().trim());
        acao.setMercado(mercado);
        acao.setMoeda(mercado == com.carteiraacoesbackend.domains.enums.Mercado.BRASIL ? Moeda.BRL : Moeda.USD);
        acao.setCotacaoAtual(cotacao.preco());
        acao.setDataHoraCotacao(cotacao.dataHora());
        // Força a verificação da unicidade do ticker antes de a compra criar posição ou operação.
        return repository.saveAndFlush(acao);
    }

    public AcaoResponse buscarPorId(Long id) { return mapper.toResponse(obterEntidade(id)); }
    public AcaoResponse buscarPorTicker(String ticker) {
        return mapper.toResponse(repository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> ApiException.notFound("ACAO_NAO_ENCONTRADA", "Ação não encontrada.")));
    }
    public Page<AcaoResponse> listar(Pageable pageable) { return repository.findAll(pageable).map(mapper::toResponse); }

    @Transactional
    public AcaoResponse atualizarCotacao(Long id) {
        Acao acao = obterEntidade(id);
        Cotacao cotacao = cotacaoAdapter.consultar(acao.getTicker(), acao.getMercado());
        validarCotacao(cotacao);
        acao.setCotacaoAtual(cotacao.preco());
        acao.setDataHoraCotacao(cotacao.dataHora().withOffsetSameInstant(ZoneOffset.UTC));
        return mapper.toResponse(repository.save(acao));
    }

    private void validarCotacao(Cotacao cotacao) {
        if (cotacao == null || cotacao.preco() == null || cotacao.preco().signum() <= 0 || cotacao.dataHora() == null) {
            throw ApiException.external(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "EXTERNAL_API_INVALID_RESPONSE", "O provedor externo retornou uma cotação inválida.");
        }
    }

    private void validarDadosCadastro(Cotacao cotacao) {
        validarCotacao(cotacao);
        if (cotacao.nomeEmpresa() == null || cotacao.nomeEmpresa().isBlank()) {
            throw ApiException.external(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "EXTERNAL_API_INVALID_RESPONSE", "O provedor externo não retornou o nome da empresa.");
        }
    }

    public Acao obterEntidade(Long id) { return repository.findById(id)
            .orElseThrow(() -> ApiException.notFound("ACAO_NAO_ENCONTRADA", "Ação não encontrada.")); }
}
