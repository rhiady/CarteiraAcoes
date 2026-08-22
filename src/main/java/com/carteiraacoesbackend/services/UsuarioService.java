package com.carteiraacoesbackend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carteiraacoesbackend.domains.Usuario;
import com.carteiraacoesbackend.dto.UsuarioRequest;
import com.carteiraacoesbackend.dto.UsuarioResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.mappers.UsuarioMapper;
import com.carteiraacoesbackend.repositories.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw ApiException.conflict("EMAIL_DUPLICADO", "Já existe um usuário com este e-mail.");
        }
        Usuario usuario = mapper.toEntity(new UsuarioRequest(request.nome().trim(), email, request.senha()));
        return mapper.toResponse(repository.save(usuario));
    }

    public UsuarioResponse buscarPorId(Long id) {
        return mapper.toResponse(obterEntidade(id));
    }

    public Page<UsuarioResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public Usuario obterEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));
    }
}
