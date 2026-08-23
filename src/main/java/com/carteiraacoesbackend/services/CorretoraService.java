package com.carteiraacoesbackend.services;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.carteiraacoesbackend.domains.Corretora;
import com.carteiraacoesbackend.dto.CorretoraRequest;
import com.carteiraacoesbackend.dto.CorretoraResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.facades.CnpjFacade;
import com.carteiraacoesbackend.facades.CvmFacade;
import com.carteiraacoesbackend.mappers.CorretoraMapper;
import com.carteiraacoesbackend.repositories.CorretoraRepository;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCnpjResponse;
import com.carteiraacoesbackend.dto.integrations.BrasilApiCvmBrokerResponse;

@Service
@Transactional(readOnly = true)
public class CorretoraService {
    private final CorretoraRepository repository; private final CnpjFacade cnpjFacade; private final CvmFacade cvmFacade; private final CorretoraMapper mapper;
    public CorretoraService(CorretoraRepository repository, CnpjFacade cnpjFacade, CvmFacade cvmFacade, CorretoraMapper mapper) { this.repository = repository; this.cnpjFacade = cnpjFacade; this.cvmFacade = cvmFacade; this.mapper = mapper; }
    @Transactional public CorretoraResponse criar(CorretoraRequest request) {
        String cnpj = request.cnpj().replaceAll("\\D", "");
        if (repository.existsByCnpj(cnpj)) throw ApiException.conflict("CNPJ_DUPLICADO", "Já existe uma corretora com este CNPJ.");
        BrasilApiCnpjResponse empresa = cnpjFacade.consultar(cnpj); BrasilApiCvmBrokerResponse registroCvm = cvmFacade.consultarCorretora(cnpj);
        Corretora c = new Corretora(); c.setCnpj(cnpj); c.setRazaoSocial(texto(empresa.razaoSocial())); c.setNomeFantasia(texto(empresa.nomeFantasia())); c.setEmail(texto(empresa.email())); c.setTelefone(texto(empresa.telefone()));
        c.setCep(texto(empresa.cep()).replaceAll("\\D", "")); c.setLogradouro(texto(empresa.logradouro())); c.setNumero(texto(empresa.numero())); c.setComplemento(empresa.complemento()); c.setBairro(texto(empresa.bairro())); c.setCidade(texto(empresa.cidade())); c.setUf(texto(empresa.uf())); c.setSituacaoCadastral(texto(empresa.situacaoCadastral())); c.setRegistroCvm(texto(registroCvm.codigoCvm())); c.setDataValidacaoCvm(OffsetDateTime.now(ZoneOffset.UTC));
        return mapper.toResponse(repository.save(c));
    }
    public CorretoraResponse buscarPorId(Long id) { return mapper.toResponse(obter(id)); }
    public CorretoraResponse buscarPorCnpj(String cnpj) { return mapper.toResponse(repository.findByCnpj(cnpj.replaceAll("\\D", "")).orElseThrow(() -> ApiException.notFound("CORRETORA_NAO_ENCONTRADA", "Corretora não encontrada."))); }
    public Page<CorretoraResponse> listar(Pageable pageable) { return repository.findAll(pageable).map(mapper::toResponse); }
    private Corretora obter(Long id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("CORRETORA_NAO_ENCONTRADA", "Corretora não encontrada.")); }
    private String texto(String value) { if (value == null || value.isBlank()) throw ApiException.unprocessable("DADOS_EXTERNOS_INCOMPLETOS", "A validação externa retornou dados incompletos."); return value; }
}
