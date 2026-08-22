package com.carteiraacoesbackend.services;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.carteiraacoesbackend.domains.Corretora;
import com.carteiraacoesbackend.dto.CorretoraRequest;
import com.carteiraacoesbackend.dto.CorretoraResponse;
import com.carteiraacoesbackend.exceptions.ApiException;
import com.carteiraacoesbackend.facades.CepFacade;
import com.carteiraacoesbackend.facades.CnpjFacade;
import com.carteiraacoesbackend.facades.CvmFacade;
import com.carteiraacoesbackend.mappers.CorretoraMapper;
import com.carteiraacoesbackend.repositories.CorretoraRepository;

@Service
@Transactional(readOnly = true)
public class CorretoraService {
    private final CorretoraRepository repository; private final CnpjFacade cnpjFacade; private final CepFacade cepFacade; private final CvmFacade cvmFacade; private final CorretoraMapper mapper;
    public CorretoraService(CorretoraRepository repository, CnpjFacade cnpjFacade, CepFacade cepFacade, CvmFacade cvmFacade, CorretoraMapper mapper) { this.repository = repository; this.cnpjFacade = cnpjFacade; this.cepFacade = cepFacade; this.cvmFacade = cvmFacade; this.mapper = mapper; }
    @Transactional public CorretoraResponse criar(CorretoraRequest request) {
        String cnpj = request.cnpj().replaceAll("\\D", ""); String cep = request.cep().replaceAll("\\D", "");
        if (repository.existsByCnpj(cnpj)) throw ApiException.conflict("CNPJ_DUPLICADO", "Já existe uma corretora com este CNPJ.");
        Map<String,Object> empresa = cnpjFacade.consultar(cnpj); Map<String,Object> endereco = cepFacade.consultar(cep); Map<String,Object> registroCvm = cvmFacade.consultarCorretora(cnpj);
        Corretora c = new Corretora(); c.setCnpj(cnpj); c.setRazaoSocial(texto(empresa,"razao_social")); c.setNomeFantasia(texto(empresa,"nome_fantasia")); c.setEmail(texto(empresa,"email")); c.setTelefone(texto(empresa,"ddd_telefone_1"));
        c.setCep(cep); c.setLogradouro(texto(endereco,"street")); c.setNumero(request.numero()); c.setComplemento(request.complemento()); c.setBairro(texto(endereco,"neighborhood")); c.setCidade(texto(endereco,"city")); c.setUf(texto(endereco,"state")); c.setSituacaoCadastral(texto(empresa,"descricao_situacao_cadastral")); c.setRegistroCvm(texto(registroCvm,"codigo_cvm")); c.setDataValidacaoCvm(OffsetDateTime.now(ZoneOffset.UTC));
        return mapper.toResponse(repository.save(c));
    }
    public CorretoraResponse buscarPorId(Long id) { return mapper.toResponse(obter(id)); }
    public CorretoraResponse buscarPorCnpj(String cnpj) { return mapper.toResponse(repository.findByCnpj(cnpj.replaceAll("\\D", "")).orElseThrow(() -> ApiException.notFound("CORRETORA_NAO_ENCONTRADA", "Corretora não encontrada."))); }
    public Page<CorretoraResponse> listar(Pageable pageable) { return repository.findAll(pageable).map(mapper::toResponse); }
    private Corretora obter(Long id) { return repository.findById(id).orElseThrow(() -> ApiException.notFound("CORRETORA_NAO_ENCONTRADA", "Corretora não encontrada.")); }
    private String texto(Map<String,Object> dados, String campo) { Object value = dados.get(campo); if (value == null || value.toString().isBlank()) throw ApiException.unprocessable("DADOS_EXTERNOS_INCOMPLETOS", "A validação externa retornou dados incompletos."); return value.toString(); }
}
