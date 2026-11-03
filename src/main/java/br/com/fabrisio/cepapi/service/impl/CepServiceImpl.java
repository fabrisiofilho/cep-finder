package br.com.fabrisio.cepapi.service.impl;

import br.com.fabrisio.cepapi.dto.Cep;
import br.com.fabrisio.cepapi.dto.CepViaCep;
import br.com.fabrisio.cepapi.service.CepService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
public class CepServiceImpl implements CepService {

    private static final String VIA_CEP = "https://viacep.com.br/ws/{cep}/json/";

    private final Gson gson = new Gson();
    private final RestTemplate client;

    public CepServiceImpl(RestTemplateBuilder builder) {
        client = builder.setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Cep search(String cep) {
        if (cep == null || !cep.matches("[0-9]{5}-[0-9]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o CEP no formato 00000-000");
        }

        String digits = cep.replace("-", "");
        JsonObject payload;
        try {
            String response = client.getForObject(VIA_CEP, String.class, digits);
            payload = gson.fromJson(response, JsonObject.class);
        } catch (RestClientException | JsonParseException exception) {
            throw unavailable(exception);
        }

        if (payload != null && payload.has("erro") && payload.get("erro").isJsonPrimitive()
                && payload.get("erro").getAsBoolean()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado");
        }

        CepViaCep address;
        try {
            address = gson.fromJson(payload, CepViaCep.class);
        } catch (JsonParseException exception) {
            throw unavailable(exception);
        }
        if (address == null || address.getCep() == null || !digits.equals(address.getCep().replace("-", ""))
                || address.getLocalidade() == null || address.getLocalidade().isBlank()
                || address.getUf() == null || !address.getUf().matches("[A-Z]{2}")) {
            throw unavailable(null);
        }

        return new Cep(digits, address.getLogradouro(), address.getComplemento(),
                address.getBairro(), address.getLocalidade(), address.getUf());
    }

    private ResponseStatusException unavailable(Exception cause) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha no provedor de CEP", cause);
    }
}
