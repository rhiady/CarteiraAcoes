package com.carteiraacoesbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carteiraacoesbackend.domain.Operacao;

public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    Page<Operacao> findByCarteiraIdOrderByDataHoraDesc(Long carteiraId, Pageable pageable);
}
