let loggedUserId = parseInt(sessionStorage.getItem('loggedUserId'));
let messagesInterval = null;

if (loggedUserId === -1) {
    alert("Usuário não logado corretamente!");
    //window.location.href = "login.html";
}

async function carregarUsuarios() {
  try {
    const resposta = await fetch("http://localhost:4567/api/usuarios");
    if (!resposta.ok) throw new Error('Erro na requisição');

    const lista = await resposta.json();
    const ul = document.getElementById("userList");
    ul.innerHTML = "";

    lista.forEach(usuario => {
      const li = document.createElement("li");
      li.classList.add(usuario.online ? 'online' : 'offline');

      const img = document.createElement("img");
      img.src = `../img/${usuario.imagem}`;
      img.alt = usuario.nome;

      const infoDiv = document.createElement("div");
      infoDiv.classList.add("user-info");

      const nome = document.createElement("div");
      nome.classList.add("user-name");
      nome.textContent = usuario.nome;

      const status = document.createElement("div");
      status.classList.add("user-status");
      status.textContent = usuario.online ? "Online" : "Offline";

      infoDiv.appendChild(nome);
      infoDiv.appendChild(status);

      li.appendChild(img);
      li.appendChild(infoDiv);
      li.onclick = () => abrirChat(usuario);
      ul.appendChild(li);
    });
  } catch (err) {
    alert("Erro ao carregar usuários.");
    console.error(err);
  }
}

function abrirChat(usuario) {
  currentUser = usuario;

  const chatArea = document.getElementById("areaChat");
  chatArea.innerHTML = `
    <div class="chat-header">Chat com ${usuario.nome}</div>
    <div class="chat-messages" id="chatMessages"></div>
    <div class="chat-input">
      <input type="text" id="messageInput" placeholder="Digite uma mensagem..." />
      <button id="sendButton">Enviar</button>
    </div>
  `;

  document.getElementById('sendButton').addEventListener('click', enviarMensagem);

  carregarMensagens();

  document.getElementById("messageInput").focus();
  document.getElementById("messageInput").addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      enviarMensagem();
    }
  });

  if (messagesInterval) clearInterval(messagesInterval);
  messagesInterval = setInterval(carregarMensagens, 2000);
}

// funciona
function carregarMensagens() {
  if (!currentUser || typeof loggedUserId === 'undefined') return;

  fetch(`http://localhost:4567/api/messages/${loggedUserId}/${currentUser.id}`)
    .then(response => response.json())
    .then(data => {
      const chatMessages = document.getElementById("chatMessages");
      chatMessages.innerHTML = "";
      data.forEach(msg => {
        const className = msg.userId === loggedUserId ? 'sent' : 'received';
        const time = msg.horario ? new Date(msg.horario).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : '';
        
        const messageDiv = document.createElement("div");
        messageDiv.className = `message ${className}`;
        
        messageDiv.innerHTML = `
          <div class="message-username">${msg.userId === loggedUserId ? 'Voce' : currentUser.nome}</div>
          <div class="message-text">${escapeHtml(msg.texto)}</div>
          <div class="message-time">${time}</div>
        `;
        
        chatMessages.appendChild(messageDiv);
      });
      chatMessages.scrollTop = chatMessages.scrollHeight;
    })
    .catch(err => console.error("Erro ao carregar mensagens:", err));
}

function enviarMensagem() {
  const input = document.getElementById("messageInput");
  const texto = input.value.trim();
  if (!texto || !currentUser || typeof loggedUserId === 'undefined') return;

  const payload = {
    remetenteId: loggedUserId,
    destinatarioId: currentUser.id,
    texto: texto
  };

  fetch("http://localhost:4567/api/messages/send", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  })
    .then(response => response.json())
    .then(data => {
    console.log("Resposta do envio:", data);
    if (data.success) {
        input.value = "";
        carregarMensagens();
    } else {
        alert("Erro ao enviar mensagem.");
    }
    })
    .catch(err => {
      console.error("Erro ao enviar mensagem:", err);
    });
}

function escapeHtml(text) {
  return text.replace(/[&<>"']/g, function (match) {
    const map = {
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;"
    };
    return map[match];
  });
}


function voltar() {
  window.location.href = "login.html";
}

document.addEventListener('DOMContentLoaded', () => {
  if (!loggedUserId) {
    window.location.href = "login.html";
    return;
  }
  
  carregarUsuarios();
});