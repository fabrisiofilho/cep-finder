package br.com.fabrisio.cepapi;

import br.com.fabrisio.cepapi.controller.CepController;
import br.com.fabrisio.cepapi.controller.CepErrorHandler;
import br.com.fabrisio.cepapi.service.impl.CepServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CepApiContractTests {

    private static final String VIA_CEP = "https://viacep.com.br/ws/01001000/json/";
    private MockRestServiceServer provider;
    private MockMvc api;

    @BeforeEach
    void setUp() {
        var builder = new RestTemplateBuilder().additionalCustomizers(
                client -> provider = MockRestServiceServer.bindTo(client).build());
        api = MockMvcBuilders.standaloneSetup(new CepController(new CepServiceImpl(builder)))
                .setControllerAdvice(new CepErrorHandler())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"01001-000"})
    void returnsNormalizedAddress(String cep) throws Exception {
        provider.expect(requestTo(VIA_CEP)).andRespond(withSuccess("""
                {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"",
                 "bairro":"Sé","localidade":"São Paulo","uf":"SP"}
                """, MediaType.APPLICATION_JSON));
        api.perform(get("/cep.api/{cep}", cep))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001000"))
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.localidade").value("São Paulo"));
        provider.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "01001000", "01001-0000", "ABCDE-FGH"})
    void rejectsInvalidCepBeforeCallingProvider(String cep) throws Exception {
        api.perform(get("/cep.api/{cep}", cep))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_CEP"))
                .andExpect(jsonPath("$.message").isNotEmpty());
        provider.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"erro\":true}", "{\"erro\":\"true\"}"})
    void mapsMissingCepToNotFound(String body) throws Exception {
        provider.expect(requestTo(VIA_CEP)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        api.perform(get("/cep.api/01001-000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CEP_NOT_FOUND"));
        provider.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<html>Erro</html>", "{\"cep\":\"01001-000\"}", "{\"cep\":\"12345-678\"}"})
    void mapsUnexpectedPayloadToBadGateway(String body) throws Exception {
        provider.expect(requestTo(VIA_CEP)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        api.perform(get("/cep.api/01001-000"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PROVIDER_UNAVAILABLE"));
        provider.verify();
    }

    @Test
    void mapsProviderHttpFailureToBadGateway() throws Exception {
        provider.expect(requestTo(VIA_CEP)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        api.perform(get("/cep.api/01001-000"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PROVIDER_UNAVAILABLE"));
        provider.verify();
    }
}
