package br.com.fabrisio.cepapi.controller;

import br.com.fabrisio.cepapi.dto.Cep;
import br.com.fabrisio.cepapi.service.CepService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/cep.api")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    public Cep getCEP(@PathVariable String cep){
        return cepService.search(cep);
    }

}
