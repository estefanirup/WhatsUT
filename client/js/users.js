let loggedUserId = parseInt(sessionStorage.getItem('loggedUserId'));
let currentUser = null;
let currentGrupo = null;
let isGrupo = false;
let messagesInterval = null;

if (!loggedUserId || loggedUserId === -1) {
  alert("Usuário não logado corretamente!");
  window.location.href = "login.html";
}

document.addEventListener('DOMContentLoaded', () => {
  carregarUsuarios();
  carregarGrupos();
});

async function carregarUsuarios() {
  try {
    const resposta = await fetch("http://localhost:4567/api/usuarios");
    const lista = await resposta.json();
    const ul = document.getElementById("userList");
    ul.innerHTML = "";

    lista.forEach(usuario => {
      const li = document.createElement("li");
      li.classList.add(usuario.online ? 'online' : 'offline');

      li.innerHTML = `
        <img src="../img/${usuario.imagem}" alt="${usuario.nome}">
        <div class="user-info">
          <div class="user-name">${usuario.nome}</div>
          <div class="user-status">${usuario.online ? "Online" : "Offline"}</div>
        </div>
      `;

      li.onclick = () => abrirChat(usuario, false);
      ul.appendChild(li);
    });
  } catch (err) {
    alert("Erro ao carregar usuários.");
    console.error(err);
  }
}

async function carregarGrupos() {
  try {
    const resposta = await fetch(`http://localhost:4567/api/grupos/${loggedUserId}`);
    const grupos = await resposta.json();
    const ul = document.getElementById("groupList");
    ul.innerHTML = "";

    grupos.forEach(grupo => {
      const li = document.createElement("li");
      li.classList.add("grupo-item");
      li.innerHTML = `<strong>#</strong> ${grupo.nome}`;
      li.onclick = () => abrirChat(grupo, true);
      ul.appendChild(li);
    });
  } catch (err) {
    alert("Erro ao carregar grupos.");
    console.error(err);
  }
}

function abrirChat(destinatario, grupo) {
  if (grupo) {
    currentGrupo = destinatario;
  }
  else {
    currentUser = destinatario;
  }
  isGrupo = grupo;

  const chatArea = document.getElementById("areaChat");
  chatArea.innerHTML = `
    <div class="chat-header">Conversando com ${grupo ? `Grupo: ${currentGrupo.nome}` : currentUser.nome}</div>
    <div class="chat-messages" id="chatMessages"></div>
    <div class="chat-input">
      <input type="text" id="messageInput" placeholder="Digite uma mensagem..." />
      <button id="sendButton">Enviar</button>
    </div>
  `;

  document.getElementById('sendButton').addEventListener('click', enviarMensagem);
  document.getElementById("messageInput").addEventListener('keypress', (e) => {
    if (e.key === 'Enter') enviarMensagem();
  });

  carregarMensagens();

  if (messagesInterval) clearInterval(messagesInterval);
  messagesInterval = setInterval(carregarMensagens, 2000);
}

async function carregarMensagens() {
  if (!currentUser && !currentGrupo) return;

  const url = isGrupo
    ? `http://localhost:4567/api/grupos/mensagens/${currentGrupo.id}`
    : `http://localhost:4567/api/messages/${loggedUserId}/${currentUser.id}`;

  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      throw new TypeError("Response is not JSON");
    }

    const mensagens = await response.json();
    const chat = document.getElementById("chatMessages");
    chat.innerHTML = "";

    if (isGrupo) {
      const usersResponse = await fetch("http://localhost:4567/api/usuarios");
      const allUsers = await usersResponse.json();
      const userMap = {};
      allUsers.forEach(user => {
        userMap[user.id] = user.nome;
      });

      mensagens.forEach(msg => {
        const time = msg.horario ? new Date(msg.horario).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit'
        }) : '';

        const senderName = msg.userId === loggedUserId ? "Você" : (userMap[msg.userId] || `Usuário ${msg.userId}`);

        const msgDiv = document.createElement("div");
        msgDiv.className = `message ${msg.userId === loggedUserId ? 'sent' : 'received'}`;
        msgDiv.innerHTML = `
          <div class="message-username">${senderName}</div>
          <div class="message-text">${escapeHtml(msg.texto)}</div>
          <div class="message-time">${time}</div>
        `;
        chat.appendChild(msgDiv);
      });
    } else {
      // For private chats, use the existing logic
      mensagens.forEach(msg => {
        const time = msg.horario ? new Date(msg.horario).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit'
        }) : '';

        const msgDiv = document.createElement("div");
        msgDiv.className = `message ${msg.userId === loggedUserId ? 'sent' : 'received'}`;
        msgDiv.innerHTML = `
          <div class="message-username">${msg.userId === loggedUserId ? "Você" : currentUser.nome}</div>
          <div class="message-text">${escapeHtml(msg.texto)}</div>
          <div class="message-time">${time}</div>
        `;
        chat.appendChild(msgDiv);
      });
    }

    chat.scrollTop = chat.scrollHeight;
  } catch (error) {
    console.error("Error loading messages:", error);
  }
}

function enviarMensagem() {
  const input = document.getElementById("messageInput");
  const texto = input.value.trim();
  if (!texto || (!currentUser && !currentGrupo)) return;

  const url = isGrupo
    ? "http://localhost:4567/api/grupos/mensagens/enviar"
    : "http://localhost:4567/api/messages/send";

  const payload = isGrupo
    ? {
      remetenteId: loggedUserId,
      grupoId: currentGrupo.id,  // Use grupoId for group messages
      texto
    }
    : {
      remetenteId: loggedUserId,
      destinatarioId: currentUser.id,  // Use destinatarioId for private messages
      texto
    };

  fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  })
    .then(res => res.json())
    .then(res => {
      if (res.success) {
        input.value = "";
        carregarMensagens();
      } else {
        alert("Erro ao enviar mensagem");
      }
    });
}

function escapeHtml(text) {
  return text.replace(/[&<>"']/g, match => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
  })[match]);
}

async function mostrarPedidos(grupoId) {
  try {
    const resposta = await fetch(`http://localhost:4567/api/grupos/${grupoId}/pedidos?adminId=${loggedUserId}`);
    if (!resposta.ok) throw new Error("Erro ao carregar pedidos");

    const pedidos = await resposta.json();
    const modal = document.getElementById("modalPedidos");
    const lista = document.getElementById("listaPedidos");

    lista.innerHTML = pedidos.map(userId => `
            <li>
                Usuário ${userId}
                <button onclick="aprovarPedido(${grupoId}, ${userId})">Aprovar</button>
                <button onclick="rejeitarPedido(${grupoId}, ${userId})">Rejeitar</button>
            </li>
        `).join('');

    modal.style.display = 'block';
  } catch (e) {
    console.error("Erro ao mostrar pedidos:", e);
  }
}

function fecharModalPedidos() {
  document.getElementById("modalPedidos").style.display = 'none';
}

async function aprovarPedido(grupoId, userId) {
  try {
    const resposta = await fetch("http://localhost:4567/api/grupos/aprovar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ grupoId, adminId: loggedUserId, usuarioId: userId })
    });

    if (resposta.ok) {
      mostrarPedidos(grupoId);
      carregarGrupos();
    }
  } catch (e) {
    console.error("Erro ao aprovar pedido:", e);
  }
}


function voltar() {
  window.location.href = "login.html";
}

function irParaGrupos() {
  window.location.href = "grupos.html";
}

function abrirModalPedidos(pedidos, grupo) {
  const ul = document.getElementById("listaPedidos");
  ul.innerHTML = "";

  pedidos.forEach(usuario => {
    const li = document.createElement("li");
    li.textContent = usuario;

    const btnAprovar = document.createElement("button");
    btnAprovar.textContent = "Aprovar";
    btnAprovar.onclick = async () => {
      await aprovarUsuario(grupo, usuario);
      alert("Usuário aprovado!");
      ul.removeChild(li);
    };

    const btnRejeitar = document.createElement("button");
    btnRejeitar.textContent = "Rejeitar";
    btnRejeitar.onclick = async () => {
      await rejeitarUsuario(grupo, usuario);
      alert("Usuário rejeitado!");
      ul.removeChild(li);
    };

    const btnBanir = document.createElement("button");
    btnBanir.textContent = "Banir";
    btnBanir.onclick = async () => {
      await banirUsuario(grupo, usuario);
      alert("Usuário banido!");
      ul.removeChild(li);
    };

    li.appendChild(btnAprovar);
    li.appendChild(btnRejeitar);
    li.appendChild(btnBanir);
    ul.appendChild(li);
  });

  document.getElementById("modalPedidos").style.display = "block";
}

function fecharModalPedidos() {
  document.getElementById("modalPedidos").style.display = "none";
}
