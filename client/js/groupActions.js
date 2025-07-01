let loggedUser = parseInt(sessionStorage.getItem("loggedUserId"));

// Envia pedido de entrada para o grupo
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

// Verifica se o usuário atual é admin do grupo
async function verificarAdmin(grupoId) {
    try {
        const resp = await fetch(`http://localhost:4567/api/grupos/${grupoId}/admin?usuarioId=${loggedUser}`);
        const data = await resp.json();
        return data.success;
    } catch (e) {
        console.error("Erro ao verificar admin:", e);
        return false;
    }
}

// Lista os pedidos pendentes de entrada no grupo
async function listarPedidosPendentes(grupoId) {
    try {
        const resp = await fetch(`http://localhost:4567/api/grupos/${grupoId}/pedidos?adminId=${loggedUser}`);
        const data = await resp.json();
        return data || [];
    } catch (e) {
        console.error("Erro ao listar pedidos pendentes:", e);
        return [];
    }
}

// Aprova um usuário no grupo
async function aprovarUsuario(grupoId, usuarioId) {
    try {
        const resp = await fetch("http://localhost:4567/api/grupos/aprovar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ grupoId, adminId: loggedUser, usuarioId })
        });

        const data = await resp.json();
        if (data.success) {
            alert("Usuário aprovado!");
        } else {
            alert("Erro ao aprovar usuário: " + (data.error || ""));
        }
        return data.success;
    } catch (e) {
        console.error("Erro ao aprovar usuário:", e);
        return false;
    }
}

// Rejeita um pedido de entrada no grupo
async function rejeitarUsuario(grupoId, usuarioId) {
    try {
        const resp = await fetch("http://localhost:4567/api/grupos/rejeitar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ grupoId, adminId: loggedUser, usuarioId })
        });

        const data = await resp.json();
        if (data.success) {
            alert("Pedido rejeitado.");
        } else {
            alert("Erro ao rejeitar pedido: " + (data.error || ""));
        }
        return data.success;
    } catch (e) {
        console.error("Erro ao rejeitar pedido:", e);
        return false;
    }
}

// Bane um usuário do grupo
async function banirUsuario(grupoId, usuarioId) {
    try {
        const resp = await fetch("http://localhost:4567/api/grupos/banir", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                grupoId, 
                adminId: loggedUser, 
                usuarioParaBanir: usuarioId 
            })
        });

        const data = await resp.json();
        if (data.success) {
            alert("Usuário banido.");
        } else {
            alert("Erro ao banir usuário: " + (data.error || ""));
        }
        return data.success;
    } catch (e) {
        console.error("Erro ao banir usuário:", e);
        return false;
    }
}
