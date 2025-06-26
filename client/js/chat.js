document.addEventListener("DOMContentLoaded", () => {
  let usuarios = [];

  const userListEl = document.getElementById("userList");
  const chatMessagesContainer = document.getElementById("chatMessagesContainer");
  const inputMessage = document.getElementById("inputMessage");
  const btnSend = document.getElementById("btnSend");
  const searchUsers = document.getElementById("searchUsers");

  let usuarioSelecionado = null;
  let mensagens = {};

  // Busca lista de usuários do servidor via Spark
  async function carregarUsuariosDoServidor() {
    try {
      const res = await fetch("http://localhost:4567/api/usuarios");
      if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);

      usuarios = await res.json();

      // Exemplo do formato esperado:
      // usuarios = [
      //   { nome: "Marie Horwitz", imagem: "...url...", online: true, ultimaMsg: "Olá", hora: "Agora" },
      //   ...
      // ]

      // Inicializa mensagens para cada usuário (vazio inicialmente)
      usuarios.forEach(u => {
        if (!mensagens[u.nome]) {
          mensagens[u.nome] = [];
        }
      });

      renderUserList();
    } catch (error) {
      console.error("Erro ao carregar usuários do servidor:", error);
      userListEl.innerHTML = `<p class="text-danger">Erro ao carregar usuários</p>`;
    }
  }

  // Renderiza lista de usuários
  function renderUserList(filtro = "") {
    userListEl.innerHTML = "";
    usuarios
      .filter(u => u.nome.toLowerCase().includes(filtro.toLowerCase()))
      .forEach(user => {
        const li = document.createElement("li");
        li.classList.add("p-2", "border-bottom", "position-relative");

        // Corrigi: propriedade 'imagem' (no JSON) para 'img' para manter consistência,
        // ou ajuste no backend para enviar 'imagem' no JSON.
        const avatar = user.imagem || user.img || "https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-chat/ava1-bg.webp";

        li.innerHTML = `
          <a href="#" class="d-flex justify-content-between align-items-center">
            <div class="d-flex flex-row align-items-center">
              <div class="position-relative">
                <img src="${avatar}" alt="avatar" class="d-flex align-self-center me-3 rounded-circle" width="60" height="60" />
                <span class="badge-dot bg-${user.online ? "success" : "danger"}"></span>
              </div>
              <div class="pt-1">
                <p class="fw-bold mb-0">${user.nome}</p>
                <p class="small text-muted">${user.ultimaMsg || ""}</p>
              </div>
            </div>
            <div class="pt-1 text-end">
              <p class="small text-muted mb-1">${user.hora || "Agora"}</p>
            </div>
          </a>
        `;

        li.querySelector("a").addEventListener("click", (e) => {
          e.preventDefault();
          selecionarUsuario(user.nome);
        });

        userListEl.appendChild(li);
      });
  }

  // Seleciona usuário e mostra mensagens
  function selecionarUsuario(nome) {
    usuarioSelecionado = nome;
    renderMensagens();
  }

  // Renderiza as mensagens do usuário selecionado
  function renderMensagens() {
    chatMessagesContainer.innerHTML = "";
    if (!usuarioSelecionado) {
      chatMessagesContainer.innerHTML = `<p class="text-center text-muted mt-5">Selecione um usuário para iniciar o chat.</p>`;
      return;
    }

    if (!mensagens[usuarioSelecionado]) {
      mensagens[usuarioSelecionado] = [];
    }

    mensagens[usuarioSelecionado].forEach(msg => {
      const divMsg = document.createElement("div");
      divMsg.classList.add("d-flex", "flex-row", msg.enviado ? "justify-content-end" : "justify-content-start");

      const bubble = document.createElement("div");
      bubble.classList.add("message", msg.enviado ? "sent" : "received");
      bubble.textContent = msg.texto;

      const hora = document.createElement("p");
      hora.classList.add("small", msg.enviado ? "me-3" : "ms-3", "mb-3", "text-muted");
      hora.textContent = msg.hora;

      if (msg.enviado) {
        divMsg.appendChild(hora);
        divMsg.appendChild(bubble);
      } else {
        divMsg.appendChild(bubble);
        divMsg.appendChild(hora);
      }

      chatMessagesContainer.appendChild(divMsg);
    });

    chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
  }

  // Envia mensagem
  btnSend.addEventListener("click", () => {
    if (!usuarioSelecionado) {
      alert("Selecione um usuário para enviar mensagem.");
      return;
    }
    const texto = inputMessage.value.trim();
    if (!texto) return;

    mensagens[usuarioSelecionado].push({
      texto,
      enviado: true,
      hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    });

    renderMensagens();
    inputMessage.value = "";

    // Simula resposta automática depois de 1s
    setTimeout(() => {
      mensagens[usuarioSelecionado].push({
        texto: `Resposta automática de ${usuarioSelecionado}`,
        enviado: false,
        hora: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      });
      renderMensagens();
    }, 1000);
  });

  // Busca usuários na lista ao digitar
  searchUsers.addEventListener("input", e => {
    renderUserList(e.target.value);
  });

  // Inicializa carregamento dos usuários do backend
  carregarUsuariosDoServidor();
});
