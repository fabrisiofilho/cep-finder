# CEP Finder

Consulte endereços brasileiros por CEP. A página inicial reúne um formulário de busca, um painel de resultados e a documentação da API.

[Abra o CEP Finder online](https://cep-finder.onrender.com/). O primeiro acesso após um período sem uso pode levar cerca de um minuto.

## Executar localmente

Com Java 17 e Maven instalados:

```sh
mvn spring-boot:run
```

Abra [a interface local](http://localhost:8080/) e teste com **01001-000**. O Spring Boot serve a página e a API na mesma porta. A consulta requer acesso ao ViaCEP por HTTPS.

## API

Use `GET /cep.api/{cep}` com o formato obrigatório 00000-000. A rota é pública e retorna o CEP com oito dígitos no JSON.

```sh
curl http://localhost:8080/cep.api/01001-000
```

```json
{
  "cep": "01001000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP"
}
```

| HTTP | Código de erro | Significado |
| --- | --- | --- |
| 200 | — | Endereço encontrado |
| 400 | `INVALID_CEP` | CEP com formato inválido |
| 404 | `CEP_NOT_FOUND` | CEP não encontrado |
| 502 | `PROVIDER_UNAVAILABLE` | Falha de comunicação com o ViaCEP ou resposta inesperada |

Os erros de consulta retornam JSON com um código estável para integrações.

```json
{
  "status": 400,
  "code": "INVALID_CEP",
  "message": "Informe o CEP no formato 00000-000"
}
```

## Testes

```sh
mvn test
```
