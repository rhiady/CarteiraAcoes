package com.carteiraacoesbackend.resources;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.carteiraacoesbackend.dto.AcaoResponse;
import com.carteiraacoesbackend.services.AcaoService;

@RestController
@RequestMapping("/acoes")
public class AcaoResource {
    private final AcaoService service;
    public AcaoResource(AcaoService service) { this.service = service; }
    @GetMapping public Page<AcaoResponse> listar(Pageable pageable) { return service.listar(pageable); }
    @GetMapping("/{id}") public AcaoResponse buscarPorId(@PathVariable Long id) { return service.buscarPorId(id); }
    @GetMapping("/ticker/{ticker}") public AcaoResponse buscarPorTicker(@PathVariable String ticker) { return service.buscarPorTicker(ticker); }
    @org.springframework.web.bind.annotation.PostMapping("/{id}/cotacao")
    public AcaoResponse atualizarCotacao(@PathVariable Long id) { return service.atualizarCotacao(id); }
}
