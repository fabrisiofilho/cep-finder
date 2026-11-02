# Find-Cep

## Consome duas api, ViaCep e Widenet.

### Requisição e Endpoint

Não temos retrição, e publico, então apenas um get sem precisar passar nada já retornaria.

```
  Get -> Para https://get-cep.herokuapp.com/cep.api/{cep}
  Retornar um json com o Objeto CEP.
  {
    "cep": "111111111",
    "logradouro": "Rua X",
    "complemento": "de 744/745 a 1199/1200",
    "bairro": "Bairro X",
    "localidade": "Cidade X",
    "uf": "Estado X em sigla"
  }
```

### Planos futuros


Implantar um sistema de retorno de erro, que atualmente retorna nada se não encontrar nenhum Cep.

---

<!-- github-standardizer:start -->
## Overview

Brazilian postal code lookup integration using CEP APIs.

## Technology indicators

- Java / Maven

## Getting started

Check the project manifests and existing documentation for supported setup commands. No installation instructions were inferred.

<!-- github-standardizer:end -->
