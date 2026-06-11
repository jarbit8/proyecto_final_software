package com.finance.project.functional;

import com.finance.project.dtos.dtos.NewCreatePersonInfoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("functional")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonApiFunctionalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("US001 - Consultar persona registrada por email")
    public void getBootstrappedPerson() {

        ResponseEntity<String> response = restTemplate.getForEntity("/persons/miguel@gmail.com", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("miguel@gmail.com"));
    }

    @Test
    @DisplayName("US001 - Registrar nueva persona y consultarla")
    public void createPersonAndFetchIt() {

        String email = "qa" + System.currentTimeMillis() + "@test.com";
        NewCreatePersonInfoDTO info = new NewCreatePersonInfoDTO(email, "Qa Tester", "1999-05-10", "Arequipa");

        ResponseEntity<String> created = restTemplate.postForEntity("/persons", info, String.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());

        ResponseEntity<String> fetched = restTemplate.getForEntity("/persons/" + email, String.class);
        assertEquals(HttpStatus.OK, fetched.getStatusCode());
        assertTrue(fetched.getBody().contains(email));
    }

    @Test
    @DisplayName("US006 - Listar cuentas de una persona")
    public void listPersonAccounts() {

        ResponseEntity<String> response = restTemplate.getForEntity("/persons/miguel@gmail.com/accounts", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Company"));
    }

    @Test
    @DisplayName("La especificacion OpenAPI esta publicada")
    public void apiDocsArePublished() {

        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"openapi\""));
    }
}
