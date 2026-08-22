package com.carteiraacoesbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carteiraacoesbackend.domain.Carteira;

public interface CarteiraRepository extends JpaRepository<Carteira, Long> {

    Page<Carteira> findByUsuarioId(Long usuarioId, Pageable pageable);
}
