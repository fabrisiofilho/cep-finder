const form = document.querySelector('#lookup');
const input = document.querySelector('#cep');
const submit = form.querySelector('[type="submit"]');
const example = document.querySelector('#example');
const status = document.querySelector('#status');
const result = document.querySelector('#result');
const radar = document.querySelector('.radar');

input.addEventListener('input', () => {
  const digits = input.value.replace(/\D/g, '').slice(0, 8);
  input.value = digits.length > 5 ? digits.slice(0, 5) + '-' + digits.slice(5) : digits;
});

example.addEventListener('click', () => {
  input.value = '01001-000';
  form.requestSubmit();
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (submit.disabled) return;

  const cep = input.value;
  submit.disabled = true;
  example.disabled = true;
  result.hidden = true;
  radar.hidden = false;
  status.textContent = 'Consultando CEP…';

  try {
    const response = await fetch('/cep.api/' + cep);
    if (!response.ok) {
      const messages = {
        400: 'Informe o CEP no formato 00000-000.',
        404: 'CEP não encontrado.'
      };
      status.textContent = messages[response.status] ?? 'Consulta indisponível. Tente novamente.';
      return;
    }

    const address = await response.json();
    const fields = [
      ['CEP', address.cep],
      ['Logradouro', address.logradouro],
      ['Complemento', address.complemento],
      ['Bairro', address.bairro],
      ['Cidade', address.localidade],
      ['UF', address.uf]
    ];
    result.replaceChildren();
    for (const [label, value] of fields) {
      const term = document.createElement('dt');
      const detail = document.createElement('dd');
      term.textContent = label;
      detail.textContent = value || '—';
      result.append(term, detail);
    }
    radar.hidden = true;
    result.hidden = false;
    status.textContent = 'Endereço encontrado';
  } catch {
    status.textContent = 'Consulta indisponível. Tente novamente.';
  } finally {
    submit.disabled = false;
    example.disabled = false;
  }
});
