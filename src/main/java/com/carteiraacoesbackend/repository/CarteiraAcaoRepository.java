package com.carteiraacoesbackend.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carteiraacoesbackend.domain.CarteiraAcao;

public interface CarteiraAcaoRepository extends JpaRepository<CarteiraAcao, Long> {

    Optional<CarteiraAcao> findByCarteiraIdAndAcaoId(Long carteiraId, Long acaoId);

    Page<CarteiraAcao> findByCarteiraIdAndQuantidadeGreaterThan(Long carteiraId, BigDecimal quantidade, Pageable pageable);
}
