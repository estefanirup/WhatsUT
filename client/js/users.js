let loggedUserId = parseInt(sessionStorage.getItem("loggedUserId"));
let currentUser = null;
let isGrupo = false;
let messagesInterval = null;

if (!loggedUserId || loggedUserId === -1) {
  alert("Usuário não logado corretamente!");
  window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", () => {
  carregarUsuarios();
  carregarGrupos();
});

async function carregarUsuarios() {
  try {
    const resposta = await fetch("http://localhost:4567/api/usuarios");
    const lista = await resposta.json();
    const ul = document.getElementById("userList");
    ul.innerHTML = "";

    lista.forEach((usuario) => {
      const li = document.createElement("li");
      li.classList.add(usuario.online ? "online" : "offline");

      li.innerHTML = `
        <img src="../img/${usuario.imagem}" alt="${usuario.nome}">
        <div class="user-info">
          <div class="user-name">${usuario.nome}</div>
          <div class="user-status">${
            usuario.online ? "Online" : "Offline"
          }</div>
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

    grupos.forEach((nomeGrupo) => {
      const li = document.createElement("li");
      li.classList.add("grupo-item");
      li.innerHTML = `<strong>#</strong> ${nomeGrupo}`;
      li.onclick = () => abrirChat({ nome: nomeGrupo, id: nomeGrupo }, true);
      ul.appendChild(li);
    });
  } catch (err) {
    alert("Erro ao carregar grupos.");
    console.error(err);
  }
}

function abrirChat(destinatario, grupo) {
  currentUser = destinatario;
  isGrupo = grupo;

  const chatArea = document.getElementById("areaChat");
  const template = document
    .getElementById("chat-template")
    .content.cloneNode(true);
  template.querySelector(".chat-header").textContent = `Conversando com ${
    grupo ? `Grupo: ${destinatario.nome}` : destinatario.nome
  }`;
  chatArea.innerHTML = ` `;
  chatArea.appendChild(template);
  document
    .getElementById("sendButton")
    .addEventListener("click", enviarMensagem);
  document.getElementById("messageInput").addEventListener("keypress", (e) => {
    if (e.key === "Enter") enviarMensagem();
  });
  document
    .getElementById("fileInput")
    .addEventListener("change", enviarArquivo);
  carregarMensagens();

  if (messagesInterval) clearInterval(messagesInterval);
  messagesInterval = setInterval(carregarMensagens, 2000);
}

function carregarMensagens() {
  if (!currentUser) return;

  const url = isGrupo
    ? `http://localhost:4567/api/messages/grupo/${currentUser.id}`
    : `http://localhost:4567/api/messages/${loggedUserId}/${currentUser.id}`;

  fetch(url)
    .then((res) => res.json())
    .then((mensagens) => {
      const chat = document.getElementById("chatMessages");
      chat.innerHTML = "";
      mensagens.forEach((msg) => {
        const time = msg.horario
          ? new Date(msg.horario).toLocaleTimeString([], {
              hour: "2-digit",
              minute: "2-digit",
            })
          : "";
        const msgDiv = document.createElement("div");
        msgDiv.className = `message ${
          msg.userId === loggedUserId ? "sent" : "received"
        }`;

        let contentHtml = `<div class="message-text">${escapeHtml(
          msg.texto
        )}</div>`;

        // Verifica se é uma mensagem de arquivo
        if (msg.fileContentBase64 && msg.fileContentBase64.length > 0) {
          // Para imagens, exibe a imagem
          if (msg.fileMimeType && msg.fileMimeType.startsWith("image/")) {
            contentHtml += `<img src="data:${msg.fileMimeType};base64,${
              msg.fileContentBase64
            }" class="message-image" alt="${escapeHtml(
              msg.fileName || "Arquivo de Imagem"
            )}">`;
          } else {
            // Para outros tipos de arquivo, um link de download
            contentHtml += `<a href="data:${msg.fileMimeType};base64,${
              msg.fileContentBase64
            }" download="${escapeHtml(
              msg.fileName || "arquivo"
            )}" class="message-file-link">
                                  &#128190; Baixar ${escapeHtml(
                                    msg.fileName || "Arquivo"
                                  )}
                                </a>`;
          }
        }

        msgDiv.innerHTML = `
          <div class="message-username">${
            msg.userId === loggedUserId
              ? "Você"
              : msg.remetenteNome || "Usuário"
          }</div>
          ${contentHtml}
          <div class="message-time">${time}</div>
        `;
        chat.appendChild(msgDiv);
      });
      chat.scrollTop = chat.scrollHeight;
    })
    .catch((error) => {
      console.error("Erro ao carregar mensagens:", error);
    });
}

function enviarMensagem() {
  const input = document.getElementById("messageInput");
  const texto = input.value.trim();
  const fileInput = document.getElementById("fileInput");
  const file = fileInput.files[0]; // Verifica se há um arquivo selecionado

  // Se houver um arquivo, a função enviarArquivo já cuidará do envio
  if (file) {
    enviarArquivo(); // Chama a função específica para arquivo
    return; // Sai daqui para evitar duplicidade
  }

  if (!texto || !currentUser) return;

  const url = isGrupo
    ? "http://localhost:4567/api/messages/send/grupo"
    : "http://localhost:4567/api/messages/send";

  const payload = isGrupo
    ? { remetenteId: loggedUserId, grupoId: currentUser.id, texto }
    : { remetenteId: loggedUserId, destinatarioId: currentUser.id, texto };

  fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  })
    .then((res) => res.json())
    .then((res) => {
      if (res.success) {
        input.value = "";
        carregarMensagens();
      } else {
        alert("Erro ao enviar mensagem");
      }
    })
    .catch((error) => {
      alert("Erro ao enviar mensagem.");
      console.error(error);
    });
}
function enviarArquivo() {
  const fileInput = document.getElementById("fileInput");
  const file = fileInput.files[0];
  if (!file || !currentUser) return;

  // Limpa o input do arquivo para permitir novo envio do mesmo arquivo
  fileInput.value = "";

  const reader = new FileReader();
  reader.onload = async (event) => {
    const fileContentBase64 = event.target.result.split(",")[1]; // Pega apenas a parte Base64
    const texto = document.getElementById("messageInput").value.trim(); // Pode ser uma legenda

    const payload = {
      remetenteId: loggedUserId,
      destinatarioId: currentUser.id, // Supondo conversa privada para arquivo
      texto: texto,
      fileName: file.name,
      fileMimeType: file.type,
      fileContentBase64: fileContentBase64,
    };

    try {
      const res = await fetch("http://localhost:4567/api/messages/send/file", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (data.success) {
        document.getElementById("messageInput").value = ""; // Limpa a legenda
        carregarMensagens();
      } else {
        alert("Erro ao enviar arquivo");
      }
    } catch (e) {
      alert("Erro ao enviar arquivo.");
      console.error(e);
    }
  };
  reader.readAsDataURL(file); // Lê o arquivo como URL de dados (inclui Base64)
}

function escapeHtml(text) {
  return text.replace(
    /[&<>"']/g,
    (match) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;",
      }[match])
  );
}

function voltar() {
  window.location.href = "login.html";
}

function irParaGrupos() {
  window.location.href = "grupos.html";
}
