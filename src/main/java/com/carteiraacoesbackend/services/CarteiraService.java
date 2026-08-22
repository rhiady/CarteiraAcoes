package com.carteiraacoesbackend.services;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carteiraacoesbackend.domains.Carteira;
import com.carteiraacoesbackend.domains.Usuario;
import com.carteiraacoesbackend.dto.CarteiraAcaoResponse;
import com.carteiraacoesbackend.dto.CarteiraRequest;
import com.carteiraacoesbackend.dto.CarteiraResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.mappers.CarteiraAcaoMapper;
import com.carteiraacoesbackend.mappers.CarteiraMapper;
import com.carteiraacoesbackend.repositories.CarteiraAcaoRepository;
import com.carteiraacoesbackend.repositories.CarteiraRepository;

@Service
@Transactional(readOnly = true)
public class CarteiraService {

    private final CarteiraRepository repository;
    private final CarteiraAcaoRepository carteiraAcaoRepository;
    private final UsuarioService usuarioService;
    private final CarteiraMapper mapper;
    private final CarteiraAcaoMapper carteiraAcaoMapper;

    public CarteiraService(CarteiraRepository repository, CarteiraAcaoRepository carteiraAcaoRepository,
                           UsuarioService usuarioService, CarteiraMapper mapper,
                           CarteiraAcaoMapper carteiraAcaoMapper) {
        this.repository = repository;
        this.carteiraAcaoRepository = carteiraAcaoRepository;
        this.usuarioService = usuarioService;
        this.mapper = mapper;
        this.carteiraAcaoMapper = carteiraAcaoMapper;
    }

    @Transactional
    public CarteiraResponse criar(CarteiraRequest request) {
        Usuario usuario = usuarioService.obterEntidade(request.usuarioId());
        Carteira carteira = new Carteira();
        carteira.setNome(request.nome().trim());
        carteira.setUsuario(usuario);
        return mapper.toResponse(repository.save(carteira));
    }

    public CarteiraResponse buscarPorId(Long id) {
        return mapper.toResponse(obterEntidade(id));
    }

    public Page<CarteiraResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public Page<CarteiraResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        usuarioService.obterEntidade(usuarioId);
        return repository.findByUsuarioId(usuarioId, pageable).map(mapper::toResponse);
    }

    public Page<CarteiraAcaoResponse> listarPosicoesAtivas(Long carteiraId, Pageable pageable) {
        obterEntidade(carteiraId);
        return carteiraAcaoRepository.findByCarteiraIdAndQuantidadeGreaterThan(carteiraId, BigDecimal.ZERO, pageable)
                .map(carteiraAcaoMapper::toResponse);
    }

    public Carteira obterEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("CARTEIRA_NAO_ENCONTRADA", "Carteira não encontrada."));
    }
}
