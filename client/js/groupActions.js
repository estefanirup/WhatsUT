let loggedUser = parseInt(sessionStorage.getItem("loggedUserId"));

async function entrarNoGrupo(nomeGrupo) {
  const resp = await fetch("http://localhost:4567/api/grupos/entrar", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nomeGrupo, usuario: "" + loggedUser })
  });

  const data = await resp.json();
  if (data.sucesso) {
    alert("Pedido de entrada enviado para o grupo " + nomeGrupo);
  } else {
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

async function banirUsuario(nomeGrupo, usuario) {
  const resp = await fetch("http://localhost:4567/api/grupos/banir", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nomeGrupo, admin: "" + loggedUser, usuario })
  });
  const data = await resp.json();
  return data.sucesso;
}
