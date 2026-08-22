package com.carteiraacoesbackend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carteiraacoesbackend.domains.Carteira;

public interface CarteiraRepository extends JpaRepository<Carteira, Long> {

    Page<Carteira> findByUsuarioId(Long usuarioId, Pageable pageable);
}
