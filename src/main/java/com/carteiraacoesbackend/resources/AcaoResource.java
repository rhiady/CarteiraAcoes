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

import com.carteiraacoesbackend.dto.AcaoRequest;
import com.carteiraacoesbackend.dto.AcaoResponse;
import com.carteiraacoesbackend.services.AcaoService;

@RestController
@RequestMapping("/acoes")
public class AcaoResource {
    private final AcaoService service;
    public AcaoResource(AcaoService service) { this.service = service; }
    @PostMapping public ResponseEntity<AcaoResponse> criar(@Valid @RequestBody AcaoRequest request) {
        AcaoResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping public Page<AcaoResponse> listar(Pageable pageable) { return service.listar(pageable); }
    @GetMapping("/{id}") public AcaoResponse buscarPorId(@PathVariable Long id) { return service.buscarPorId(id); }
    @GetMapping("/ticker/{ticker}") public AcaoResponse buscarPorTicker(@PathVariable String ticker) { return service.buscarPorTicker(ticker); }
}
