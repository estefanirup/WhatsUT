let loggedUser = parseInt(sessionStorage.getItem("loggedUserId"));

async function entrarNoGrupo(grupoId) {
    try {
        const resp = await fetch("http://localhost:4567/api/grupos/entrar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ grupoId, usuarioId: loggedUser })
        });

        const data = await resp.json();
        if (data.success) {
            alert("Pedido de entrada enviado para o grupo");
        } else {
            alert("Erro ao solicitar entrada no grupo: " + (data.error || ""));
        }
    } catch (e) {
        console.error("Erro ao entrar no grupo:", e);
        alert("Erro ao solicitar entrada no grupo.");
    }
}

async function verificarAdmin(nomeGrupo) {
  const resp = await fetch("http://localhost:4567/api/grupos/ehadmin", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nomeGrupo, usuario: "" + loggedUser })
  });
  const data = await resp.json();
  return data.sucesso;
}

async function listarPedidosPendentes(nomeGrupo) {
  const resp = await fetch("http://localhost:4567/api/grupos/pedidos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nomeGrupo, admin: "" + loggedUser })
  });
  const data = await resp.json();
  return data.pedidos || [];
}

async function aprovarUsuario(nomeGrupo, usuario) {
  const resp = await fetch("http://localhost:4567/api/grupos/aprovar", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nomeGrupo, admin: "" + loggedUser, usuario })
  });
  const data = await resp.json();
  return data.sucesso;
}

async function rejeitarUsuario(nomeGrupo, usuario) {
  const resp = await fetch("http://localhost:4567/api/grupos/rejeitar", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nomeGrupo, admin: "" + loggedUser, usuario })
  });
  const data = await resp.json();
  return data.sucesso;
}

async function banirUsuario(grupoId, usuarioId) {
    try {
        const resp = await fetch("http://localhost:4567/api/grupos/banir", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                grupoId: grupoId, 
                adminId: loggedUser, 
                usuarioParaBanir: usuarioId 
            })
        });
        
        const data = await resp.json();
        return data.success;
    } catch (e) {
        console.error("Erro ao banir usuário:", e);
        return false;
    }
}
