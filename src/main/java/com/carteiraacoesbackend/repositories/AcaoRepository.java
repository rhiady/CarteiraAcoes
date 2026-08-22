package com.carteiraacoesbackend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carteiraacoesbackend.domains.Acao;

public interface AcaoRepository extends JpaRepository<Acao, Long> {

    Optional<Acao> findByTickerIgnoreCase(String ticker);

    boolean existsByTickerIgnoreCase(String ticker);
}
