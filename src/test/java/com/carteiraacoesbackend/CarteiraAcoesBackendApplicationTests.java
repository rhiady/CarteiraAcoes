package com.carteiraacoesbackend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@ActiveProfiles("test")
class CarteiraAcoesBackendApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void appliesFlywayMigrationsToTheTestDatabase() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'PUBLIC'
                  AND LOWER(table_name) IN ('usuario', 'corretora', 'acao', 'carteira',
                                            'carteira_acao', 'operacao', 'flyway_schema_history')
                """, Integer.class);

        assertThat(tableCount).isEqualTo(7);
    }

}
