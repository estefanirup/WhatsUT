let loggedUserId = parseInt(sessionStorage.getItem('loggedUserId'));
let currentUser = null;
let currentGrupo = null;
let isGrupo = false;
let messagesInterval = null;
let content;

if (!loggedUserId || loggedUserId === -1) {
  alert("Usuário não logado corretamente!");
  window.location.href = "login.html";
}

document.addEventListener('DOMContentLoaded', () => {
  carregarUsuarios();
  carregarGrupos();

  document.addEventListener('click', (e) => {
    if (e.target && e.target.id === 'fileButton') {
      document.getElementById('fileInput').click();
    }
  });

  document.addEventListener('change', (e) => {
    if (e.target && e.target.id === 'fileInput') {
      enviarArquivo(e.target.files[0]);
      e.target.value = '';
    }
  });
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
    const resposta = await fetch("http://localhost:4567/api/grupos");
    const grupos = await resposta.json();
    const ul = document.getElementById("groupList");
    ul.innerHTML = "";

    for (const grupo of grupos) {
      const isParticipante = await verificarParticipante(grupo.id);
      const isAdmin = await verificarAdmin(grupo.id);

      const li = document.createElement("li");
      li.classList.add("grupo-item");
      li.innerHTML = `<strong>#</strong> ${grupo.nome}`;

      if (isParticipante) {
        li.style.cursor = "pointer";
        li.onclick = () => abrirChat(grupo, true);

        if (isAdmin) {
          const btnPedidos = document.createElement("button");
          btnPedidos.textContent = "👥";
          btnPedidos.classList.add("btn-grupo");
          btnPedidos.onclick = (e) => {
            e.stopPropagation();
            mostrarPedidos(grupo.id);
          };
          li.appendChild(btnPedidos);

          // Botão Membros para abrir modalMembros
          const btnMembros = document.createElement("button");
          btnMembros.textContent = "👤";  // ou "👥" se quiser
          btnMembros.classList.add("btn-grupo");
          btnMembros.style.marginLeft = "5px";
          btnMembros.onclick = (e) => {
            e.stopPropagation();
            abrirModalMembros(grupo.id);
          };
          li.appendChild(btnMembros);
        }
      } else {
        li.style.cursor = "default";

        const btnPedirEntrada = document.createElement("button");
        btnPedirEntrada.textContent = "➕";
        btnPedirEntrada.classList.add("btn-grupo");
        btnPedirEntrada.onclick = async (e) => {
          e.stopPropagation();
          await entrarNoGrupo(grupo.id);
          alert("Pedido enviado para o grupo " + grupo.nome);
        };

        li.appendChild(btnPedirEntrada);
      }

      ul.appendChild(li);
    }
  } catch (err) {
    alert("Erro ao carregar grupos.");
    console.error(err);
  }
}

// Verifica se usuário participa do grupo, consultando membros do grupo
async function verificarParticipante(grupoId) {
  try {
    const resp = await fetch(`http://localhost:4567/api/grupos/${grupoId}/membros`);
    if (!resp.ok) return false;
    const membros = await resp.json();
    return membros.includes(loggedUserId);
  } catch (e) {
    console.error("Erro ao verificar participante:", e);
    return false;
  }
}

// Verifica se usuário é admin do grupo (endpoint já existe)
async function verificarAdmin(grupoId) {
  try {
    const resp = await fetch(`http://localhost:4567/api/grupos/${grupoId}/admin?usuarioId=${loggedUserId}`);
    if (!resp.ok) return false;
    const data = await resp.json();
    return data.success === true;
  } catch (e) {
    console.error("Erro ao verificar admin:", e);
    return false;
  }
}

// Exemplo simples de função para entrar no grupo (faz POST)
async function entrarNoGrupo(grupoId) {
  try {
    const resp = await fetch("http://localhost:4567/api/grupos/entrar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ grupoId, usuarioId: loggedUserId }),
    });
    const data = await resp.json();
    if (!data.success) throw new Error(data.error || "Falha ao enviar pedido");
  } catch (e) {
    alert("Erro ao pedir entrada no grupo: " + e.message);
  }
}

// Placeholder para abrir o chat (implemente conforme seu app)
function abrirChat(grupo, isGroup = false) {
  console.log("Abrir chat do grupo:", grupo.nome, "Grupo?", isGroup);
}

// Placeholder para mostrar pedidos de entrada (implemente conforme seu app)
async function mostrarPedidos(grupoId) {
  try {
    const resposta = await fetch(`http://localhost:4567/api/grupos/${grupoId}/pedidos?adminId=${loggedUserId}`);
    const pedidos = await resposta.json();

    const lista = document.getElementById("listaPedidos");
    lista.innerHTML = "";

    if (pedidos.length === 0 || pedidos.size === 0) {
      lista.innerHTML = "<li>Nenhum pedido pendente.</li>";
    } else {
      // Se pedidos for um Set (como seu backend devolve), converta para array:
      const pedidosArray = Array.isArray(pedidos) ? pedidos : Array.from(pedidos);

      for (const usuarioId of pedidosArray) {
        const li = document.createElement("li");

        // Aqui você pode buscar o nome do usuário via API, ou já passar o nome
        // Se não tiver API, só mostra o ID mesmo:
        li.textContent = `Usuário ID: ${usuarioId}`;

        // Botões aceitar e rejeitar
        const btnAceitar = document.createElement("button");
        btnAceitar.textContent = "✔";
        btnAceitar.classList.add("btn-aceitar");
        btnAceitar.onclick = async (e) => {
          e.stopPropagation();
          await aprovarEntrada(grupoId, loggedUserId, usuarioId);
          alert("Entrada aprovada!");
          mostrarPedidos(grupoId); // atualiza a lista
        };

        const btnRejeitar = document.createElement("button");
        btnRejeitar.textContent = "✖";
        btnRejeitar.classList.add("btn-rejeitar");
        btnRejeitar.onclick = async (e) => {
          e.stopPropagation();
          await rejeitarEntrada(grupoId, loggedUserId, usuarioId);
          alert("Entrada rejeitada!");
          mostrarPedidos(grupoId); // atualiza a lista
        };

        const btnContainer = document.createElement("span");
        btnContainer.appendChild(btnAceitar);
        btnContainer.appendChild(btnRejeitar);

        li.appendChild(btnContainer);
        lista.appendChild(li);
      }
    }

    document.getElementById("modalPedidos").style.display = "block";
  } catch (err) {
    alert("Erro ao carregar pedidos pendentes.");
    console.error(err);
  }
}

function fecharModalPedidos() {
  document.getElementById("modalPedidos").style.display = "none";
}



async function abrirModalPedidos(pedidos, grupoId) {
  const ul = document.getElementById("listaPedidos");
  ul.innerHTML = "";

  // 🔁 Buscando todos os usuários para mapear ID -> nome
  const usuarios = await fetch("http://localhost:4567/api/usuarios").then(r => r.json());
  const userMap = {};
  usuarios.forEach(u => userMap[u.id] = u.nome);

  pedidos.forEach(usuarioId => {
    const li = document.createElement("li");

    const nome = userMap[usuarioId] || `ID ${usuarioId}`;
    li.textContent = `Usuário ${nome} `;

    // Botões (mantém o ID no backend)
    const btnAprovar = document.createElement("button");
    btnAprovar.textContent = "Aprovar";
    btnAprovar.onclick = async () => {
      await aprovarUsuario(grupoId, usuarioId);
      alert("Usuário aprovado!");
      ul.removeChild(li);
    };

    const btnRejeitar = document.createElement("button");
    btnRejeitar.textContent = "Rejeitar";
    btnRejeitar.onclick = async () => {
      await rejeitarUsuario(grupoId, usuarioId);
      alert("Usuário rejeitado!");
      ul.removeChild(li);
    };

    const btnBanir = document.createElement("button");
    btnBanir.textContent = "Banir";
    btnBanir.onclick = async () => {
      await banirUsuario(grupoId, usuarioId);
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


async function mostrarPedidos(grupoId) {
  try {
    // Busca os pedidos no backend
    const resp = await fetch(`http://localhost:4567/api/grupos/${grupoId}/pedidos?adminId=${loggedUserId}`);

    // Verifica se a resposta foi OK (status 200)
    if (!resp.ok) throw new Error("Erro ao carregar pedidos");

    // Pega a lista de pedidos (array de IDs ou objetos de usuários)
    const pedidos = await resp.json();

    // Abre o modal passando os pedidos e o grupo atual
    abrirModalPedidos(pedidos, grupoId);
  } catch (e) {
    console.error("Erro ao mostrar pedidos:", e);
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
      ${!grupo ? `
        <input type="file" id="fileInput" style="display: none;" />
        <button id="fileButton">Enviar Arquivo</button>
      ` : ''}
    </div>
  `;

  document.getElementById('sendButton').addEventListener('click', enviarMensagem);
  document.getElementById("messageInput").addEventListener('keypress', (e) => {
    if (e.key === 'Enter') enviarMensagem();
  });

  if (!grupo) {
    document.getElementById('fileButton').addEventListener('click', () => {
      document.getElementById('fileInput').click();
    });
    document.getElementById('fileInput').addEventListener('change', (e) => {
      if (e.target.files.length > 0) {
        enviarArquivo(e.target.files[0]);
        e.target.value = '';
      }
    });
  }

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
      mensagens.forEach(msg => {
        const time = msg.horario ? new Date(msg.horario).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit'
        }) : '';

        const msgDiv = document.createElement("div");
        msgDiv.className = `message ${msg.userId === loggedUserId ? 'sent' : 'received'}`;

        if (msg.texto.startsWith("[FILE]")) {
          const fileInfo = parseFileInfo(msg.texto);
          msgDiv.innerHTML = `
        <div class="message-username">${msg.userId === loggedUserId ? "Você" : currentUser.nome}</div>
        <div class="message-file">
          <a href="http://localhost:4567/uploads/${fileInfo.path}" download="${fileInfo.name}">
            📁 ${fileInfo.name} (${formatFileSize(fileInfo.size)})
          </a>
        </div>
        <div class="message-time">${time}</div>`;
        } else {
          msgDiv.innerHTML = `
        <div class="message-username">${msg.userId === loggedUserId ? "Você" : currentUser.nome}</div>
        <div class="message-text">${escapeHtml(msg.texto)}</div>
        <div class="message-time">${time}</div>`;
        }

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

//Enviar arquivo
async function enviarArquivo(file) {
  if (!file || (!currentUser && !currentGrupo)) return;

  const formData = new FormData();
  formData.append('file', file);
  formData.append('remetenteId', loggedUserId.toString());
  formData.append('destinatarioId', currentUser.id.toString());

  try {
    const response = await fetch("http://localhost:4567/api/messages/send-file", {
      method: "POST",
      body: formData
    });

    const result = await response.json();

    if (result.success) {
      carregarMensagens();
    } else {
      alert("Erro ao enviar arquivo: " + (result.message || "Erro desconhecido"));
    }
  } catch (error) {
    console.error("Error sending file:", error);
    alert("Erro ao enviar arquivo");
  }
}

function parseFileInfo(text) {
  const parts = text.substring(7).split("|");
  const info = {};
  parts.forEach(part => {
    const [key, value] = part.split("=");
    info[key] = value;
  });
  return info;
}

function formatFileSize(bytes) {
  if (typeof bytes !== 'number') return '';
  if (bytes < 1024) return bytes + " bytes";
  else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + " KB";
  else return (bytes / 1048576).toFixed(1) + " MB";
}


async function abrirModalMembros(grupoId) {
  // Salva grupoId em variável global para usar nos handlers
  window.grupoAtual = grupoId;

  const ul = document.getElementById("listaMembros");
  ul.innerHTML = "";

  try {
    const resp = await fetch(`http://localhost:4567/api/grupos/${grupoId}/membros`);
    const membros = await resp.json();

    for (const usuarioId of membros) {
      const li = document.createElement("li");
      li.textContent = `Usuário ID: ${usuarioId} `;

      // Botão banir
      const btnBanir = document.createElement("button");
      btnBanir.textContent = "🚫 Banir";
      btnBanir.style.marginLeft = "10px";
      btnBanir.onclick = async () => {
        if (confirm(`Tem certeza que quer banir o usuário ${usuarioId}?`)) {
          await banirUsuarioDoGrupo(grupoId, usuarioId);
          abrirModalMembros(grupoId); // Atualiza a lista
        }
      };

      li.appendChild(btnBanir);
      ul.appendChild(li);
    }

    // Mostrar modal
    document.getElementById("modalMembros").style.display = "block";

  } catch (err) {
    alert("Erro ao carregar membros do grupo.");
    console.error(err);
  }
}

async function banirUsuarioDoGrupo(grupoId, usuarioParaBanir) {
  try {
    // Usar id do admin logado - ajustar conforme seu app
    const adminId = loggedUserId;

    const resp = await fetch("http://localhost:4567/api/grupos/banir", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        grupoId: grupoId,
        adminId: adminId,
        usuarioParaBanir: usuarioParaBanir
      })
    });
    const data = await resp.json();
    if (data.success) {
      alert("Usuário banido com sucesso!");
    } else {
      alert("Falha ao banir usuário: " + (data.error || "Erro desconhecido"));
    }
  } catch (e) {
    alert("Erro ao banir usuário.");
    console.error(e);
  }
}

document.getElementById("btnAddUsuario").onclick = async () => {
  const input = document.getElementById("inputAddUsuario");

  const nome = input.value.trim();
  const usuarioId = await buscarUsuarioIdPorNome(nome);
  if (!usuarioId) {
    alert("Usuário não encontrado");
    return;
  }


  if (isNaN(usuarioId)) {
    alert("ID de usuário inválido");
    return;
  }
  const grupoId = window.grupoAtual;
  await adicionarUsuarioAoGrupo(grupoId, usuarioId);
  input.value = "";
  abrirModalMembros(grupoId); // Atualiza lista
};

async function adicionarUsuarioAoGrupo(grupoId, usuarioId) {
  try {
    const resp = await fetch("http://localhost:4567/api/grupos/entrar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        grupoId: grupoId,
        usuarioId: usuarioId
      })
    });
    const data = await resp.json();
    if (data.success) {
      alert("Usuário adicionado com sucesso!");
    } else {
      alert("Falha ao adicionar usuário: " + (data.error || "Erro desconhecido"));
    }
  } catch (e) {
    alert("Erro ao adicionar usuário.");
    console.error(e);
  }
}

function fecharModalMembros() {
  document.getElementById("modalMembros").style.display = "none";
}
