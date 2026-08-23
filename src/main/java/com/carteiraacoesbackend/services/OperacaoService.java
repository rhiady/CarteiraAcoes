package com.carteiraacoesbackend.services;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carteiraacoesbackend.domains.Acao;
import com.carteiraacoesbackend.domains.Carteira;
import com.carteiraacoesbackend.domains.CarteiraAcao;
import com.carteiraacoesbackend.domains.Operacao;
import com.carteiraacoesbackend.domains.enums.TipoOperacao;
import com.carteiraacoesbackend.dto.CompraRequest;
import com.carteiraacoesbackend.dto.OperacaoResponse;
import com.carteiraacoesbackend.dto.VendaRequest;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.integrations.CotacaoAdapter;
import com.carteiraacoesbackend.mappers.OperacaoMapper;
import com.carteiraacoesbackend.repositories.CarteiraAcaoRepository;
import com.carteiraacoesbackend.repositories.OperacaoRepository;

@Service
@Transactional(readOnly = true)
public class OperacaoService {
    private final OperacaoRepository operacoes;
    private final CarteiraAcaoRepository posicoes;
    private final CarteiraService carteiraService;
    private final AcaoService acaoService;
    private final CotacaoAdapter cotacaoAdapter;
    private final OperacaoMapper mapper;

    public OperacaoService(OperacaoRepository operacoes, CarteiraAcaoRepository posicoes,
                           CarteiraService carteiraService, AcaoService acaoService,
                           CotacaoAdapter cotacaoAdapter, OperacaoMapper mapper) {
        this.operacoes = operacoes; this.posicoes = posicoes; this.carteiraService = carteiraService;
        this.acaoService = acaoService; this.cotacaoAdapter = cotacaoAdapter; this.mapper = mapper;
    }

    @Transactional
    public OperacaoResponse comprar(CompraRequest request) {
        Carteira carteira = carteiraService.obterEntidade(request.carteiraId());
        Acao acao = acaoService.obterEntidade(request.acaoId());
        BigDecimal preco = request.precoUnitario() != null ? request.precoUnitario()
                : cotacaoAdapter.consultar(acao.getTicker(), acao.getMercado()).preco();
        CarteiraAcao posicao = posicoes.findByCarteiraIdAndAcaoId(carteira.getId(), acao.getId())
                .orElseGet(() -> novaPosicao(carteira, acao));
        posicao.setQuantidade(posicao.getQuantidade().add(request.quantidade()));
        posicoes.save(posicao);
        return mapper.toResponse(operacoes.save(novaOperacao(carteira, acao, TipoOperacao.COMPRA,
                request.quantidade(), preco, request.corretagem(), request.impostos(), request.valorAdicional())));
    }

    @Transactional
    public OperacaoResponse vender(VendaRequest request) {
        Carteira carteira = carteiraService.obterEntidade(request.carteiraId());
        Acao acao = acaoService.obterEntidade(request.acaoId());
        CarteiraAcao posicao = posicoes.findByCarteiraIdAndAcaoId(carteira.getId(), acao.getId())
                .orElseThrow(() -> ApiException.unprocessable("QUANTIDADE_INSUFICIENTE", "A carteira não possui esta ação."));
        if (posicao.getQuantidade().compareTo(request.quantidade()) < 0) {
            throw ApiException.unprocessable("QUANTIDADE_INSUFICIENTE", "Quantidade de ações insuficiente para a venda.");
        }
        BigDecimal preco = cotacaoAdapter.consultar(acao.getTicker(), acao.getMercado()).preco();
        posicao.setQuantidade(posicao.getQuantidade().subtract(request.quantidade()));
        posicoes.save(posicao);
        return mapper.toResponse(operacoes.save(novaOperacao(carteira, acao, TipoOperacao.VENDA,
                request.quantidade(), preco, request.corretagem(), request.impostos(), request.valorAdicional())));
    }

    public OperacaoResponse buscarPorId(Long id) { return mapper.toResponse(operacoes.findById(id)
            .orElseThrow(() -> ApiException.notFound("OPERACAO_NAO_ENCONTRADA", "Operação não encontrada."))); }
    public Page<OperacaoResponse> historico(Long carteiraId, Pageable pageable) {
        carteiraService.obterEntidade(carteiraId);
        return operacoes.findByCarteiraIdOrderByDataHoraDesc(carteiraId, pageable).map(mapper::toResponse);
    }
    private CarteiraAcao novaPosicao(Carteira carteira, Acao acao) { CarteiraAcao p = new CarteiraAcao(); p.setCarteira(carteira); p.setAcao(acao); p.setQuantidade(BigDecimal.ZERO); return p; }
    private Operacao novaOperacao(Carteira carteira, Acao acao, TipoOperacao tipo, BigDecimal quantidade, BigDecimal preco,
                                  BigDecimal corretagem, BigDecimal impostos, BigDecimal adicional) {
        Operacao op = new Operacao(); op.setCarteira(carteira); op.setAcao(acao); op.setTipo(tipo); op.setQuantidade(quantidade); op.setPrecoUnitario(preco);
        op.setCorretagem(corretagem); op.setImpostos(impostos); op.setValorAdicional(adicional); return op;
    }
}
