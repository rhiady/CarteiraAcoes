package com.carteiraacoesbackend.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogoResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsUserAndDoesNotExposePassword() throws Exception {
        mockMvc.perform(post("/usuarios").contentType("application/json")
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@teste.com\",\"senha\":\"segredo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@teste.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void rejectsDuplicateEmailAndReturnsStandardError() throws Exception {
        String body = "{\"nome\":\"Bruno\",\"email\":\"bruno@teste.com\",\"senha\":\"segredo\"}";
        mockMvc.perform(post("/usuarios").contentType("application/json").content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/usuarios").contentType("application/json").content(body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("EMAIL_DUPLICADO"));
    }

    @Test
    void createsPortfolioForExistingUserAndPaginatesUserPortfolios() throws Exception {
        String userResponse = mockMvc.perform(post("/usuarios").contentType("application/json")
                        .content("{\"nome\":\"Carla\",\"email\":\"carla@teste.com\",\"senha\":\"segredo\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long userId = Long.parseLong(userResponse.replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));

        mockMvc.perform(post("/carteiras").contentType("application/json")
                        .content("{\"nome\":\"Longo Prazo\",\"usuarioId\":" + userId + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/usuarios/{id}/carteiras?page=0&size=20", userId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].nome").value("Longo Prazo"));
    }

    @Test
    void returnsNotFoundForUnknownPortfolio() throws Exception {
        mockMvc.perform(get("/carteiras/9999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("CARTEIRA_NAO_ENCONTRADA"));
    }
}
