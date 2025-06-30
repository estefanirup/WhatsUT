let loggedUserId = parseInt(sessionStorage.getItem('loggedUserId'));

document.addEventListener("DOMContentLoaded", () => {
    if (!loggedUserId || loggedUserId === -1) {
        window.location.href = "login.html";
        return;
    }

    carregarUsuarios();
});

async function carregarUsuarios() {
    try {
        const resposta = await fetch("http://localhost:4567/api/usuarios");
        if (!resposta.ok) throw new Error("Erro na requisição de usuários");

        const usuarios = await resposta.json();
        const ul = document.getElementById("listaUsuarios");
        ul.innerHTML = "";

        usuarios.forEach(usuario => {
            if (usuario.id !== loggedUserId) {  // Não mostrar o próprio usuário
                const li = document.createElement("li");

                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.value = usuario.id;  // CORRIGIDO: valor é o ID, não o nome
                checkbox.id = `usuario-${usuario.id}`;

                const label = document.createElement("label");
                label.htmlFor = checkbox.id;
                label.textContent = " " + usuario.nome;

                li.appendChild(checkbox);
                li.appendChild(label);
                ul.appendChild(li);
            }
        });
    } catch (e) {
        alert("Erro ao carregar usuários");
        console.error(e);
    }
}

async function criarGrupo() {
    const nomeGrupo = document.getElementById("nomeGrupo").value.trim();
    if (!nomeGrupo) {
        alert("Digite o nome do grupo.");
        return;
    }

    const selecionados = Array.from(document.querySelectorAll("#listaUsuarios input:checked"))
        .map(cb => parseInt(cb.value));

    if (selecionados.length === 0) {
        alert("Selecione ao menos um participante.");
        return;
    }

    // Inclui o criador do grupo na lista de participantes
    selecionados.push(loggedUserId);

    const payload = {
        nomeGrupo: nomeGrupo,
        criador: loggedUserId,
        participantes: selecionados
    };

    try {
        const resp = await fetch("http://localhost:4567/api/grupos", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await resp.json();
        if (data.sucesso) {
            alert("Grupo criado com sucesso!");
            window.location.href = "users.html";
        } else {
            alert("Erro ao criar grupo.");
        }
    } catch (e) {
        alert("Erro ao criar grupo.");
        console.error(e);
    }
}
