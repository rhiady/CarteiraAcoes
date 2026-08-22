package com.carteiraacoesbackend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carteiraacoesbackend.domains.Corretora;

public interface CorretoraRepository extends JpaRepository<Corretora, Long> {

    Optional<Corretora> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);
}
