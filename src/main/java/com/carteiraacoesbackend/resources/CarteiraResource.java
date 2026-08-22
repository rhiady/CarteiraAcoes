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

import com.carteiraacoesbackend.dto.CarteiraAcaoResponse;
import com.carteiraacoesbackend.dto.CarteiraRequest;
import com.carteiraacoesbackend.dto.CarteiraResponse;
import com.carteiraacoesbackend.services.CarteiraService;

@RestController
@RequestMapping
public class CarteiraResource {

    private final CarteiraService service;

    public CarteiraResource(CarteiraService service) {
        this.service = service;
    }

    @PostMapping("/carteiras")
    public ResponseEntity<CarteiraResponse> criar(@Valid @RequestBody CarteiraRequest request) {
        CarteiraResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/carteiras")
    public Page<CarteiraResponse> listar(Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/carteiras/{id}")
    public CarteiraResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/usuarios/{usuarioId}/carteiras")
    public Page<CarteiraResponse> listarPorUsuario(@PathVariable Long usuarioId, Pageable pageable) {
        return service.listarPorUsuario(usuarioId, pageable);
    }

    @GetMapping("/carteiras/{id}/acoes")
    public Page<CarteiraAcaoResponse> listarPosicoesAtivas(@PathVariable Long id, Pageable pageable) {
        return service.listarPosicoesAtivas(id, pageable);
    }
}
