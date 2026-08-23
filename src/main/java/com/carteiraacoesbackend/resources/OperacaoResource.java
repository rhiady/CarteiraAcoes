package com.carteiraacoesbackend.resources;

import java.net.URI;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.carteiraacoesbackend.dto.CompraRequest;
import com.carteiraacoesbackend.dto.OperacaoResponse;
import com.carteiraacoesbackend.dto.VendaRequest;
import com.carteiraacoesbackend.services.OperacaoService;

@RestController
@RequestMapping("/operacoes")
public class OperacaoResource {
    private final OperacaoService service;
    public OperacaoResource(OperacaoService service) { this.service = service; }
    @PostMapping("/compras") public ResponseEntity<OperacaoResponse> comprar(@Valid @RequestBody CompraRequest request) { return criada(service.comprar(request)); }
    @PostMapping("/vendas") public ResponseEntity<OperacaoResponse> vender(@Valid @RequestBody VendaRequest request) { return criada(service.vender(request)); }
    @GetMapping("/{id}") public OperacaoResponse buscar(@PathVariable Long id) { return service.buscarPorId(id); }
    @GetMapping("/carteiras/{carteiraId}") public Page<OperacaoResponse> historico(@PathVariable Long carteiraId, Pageable pageable) { return service.historico(carteiraId, pageable); }
    private ResponseEntity<OperacaoResponse> criada(OperacaoResponse response) { URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri(); return ResponseEntity.created(uri).body(response); }
}
