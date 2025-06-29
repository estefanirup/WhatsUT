async function fazerCadastro() {
    const usuario = document.getElementById('usuario').value.trim();
    const senha = document.getElementById('senha').value.trim();

    if (!usuario || !senha) {
        alert('Preencha usuário e senha.');
        return;
    }

    try {
        const response = await fetch('http://localhost:4567/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuario, senha }),
        });

        if (!response.ok) {
            alert('Erro ao conectar com o servidor: ' + response.status);
            return;
        }

        const result = await response.json();

        if (result && result.sucesso) {
            alert('Cadastro realizado com sucesso! Agora faça login.');
            window.location.href = 'login.html';
        } else if (result && result.sucesso === false) {
            alert('Usuário já existe. Tente outro.');
        } else {
            alert('Resposta inesperada do servidor.');
        }

    } catch (error) {
        alert('Erro ao conectar com o servidor.');
        console.error(error);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // inicializacao (se tiver)
});