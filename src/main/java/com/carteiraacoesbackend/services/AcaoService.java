package com.carteiraacoesbackend.services;

import java.time.OffsetDateTime;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carteiraacoesbackend.domains.Acao;
import com.carteiraacoesbackend.domains.enums.Moeda;
import com.carteiraacoesbackend.dto.AcaoRequest;
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
    public AcaoResponse criar(AcaoRequest request) {
        String ticker = request.ticker().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByTickerIgnoreCase(ticker)) {
            throw ApiException.conflict("TICKER_DUPLICADO", "Já existe uma ação com este ticker.");
        }
        Cotacao cotacao = cotacaoAdapter.consultar(ticker, request.mercado());
        Acao acao = new Acao();
        acao.setTicker(ticker);
        acao.setNomeEmpresa(request.nomeEmpresa().trim());
        acao.setMercado(request.mercado());
        acao.setMoeda(request.mercado() == com.carteiraacoesbackend.domains.enums.Mercado.BRASIL ? Moeda.BRL : Moeda.USD);
        acao.setCotacaoAtual(cotacao.preco());
        acao.setDataHoraCotacao(cotacao.dataHora());
        return mapper.toResponse(repository.save(acao));
    }

    public AcaoResponse buscarPorId(Long id) { return mapper.toResponse(obterEntidade(id)); }
    public AcaoResponse buscarPorTicker(String ticker) {
        return mapper.toResponse(repository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> ApiException.notFound("ACAO_NAO_ENCONTRADA", "Ação não encontrada.")));
    }
    public Page<AcaoResponse> listar(Pageable pageable) { return repository.findAll(pageable).map(mapper::toResponse); }
    public Acao obterEntidade(Long id) { return repository.findById(id)
            .orElseThrow(() -> ApiException.notFound("ACAO_NAO_ENCONTRADA", "Ação não encontrada.")); }
}
