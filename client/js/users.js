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

/*
function abrirChat(usuario) {
  const chatArea = document.getElementById("areaChat");
  chatArea.innerHTML = `
    <div class="chat-header">Chat com ${usuario.nome}</div>
    <div class="chat-box">
      <p><i>Mensagens ainda não implementadas.</i></p>
    </div>
    <div class="chat-input">
      <input type="text" placeholder="Digite uma mensagem..." disabled />
    </div>
  `;
}
*/

function voltar() {
  window.location.href = "login.html";
}


// carregar usuarios
document.addEventListener('DOMContentLoaded', () => {
  carregarUsuarios();
  // Olha novas mensagens
  //setInterval(verificarNovasMensagens, 5000);
});