let currentUser = null;
let messages = {};

function abrirChat(usuario) {
  currentUser = usuario;
  const chatArea = document.getElementById("areaChat");
  chatArea.innerHTML = `
    <div class="chat-header">Chat com ${usuario.nome}</div>
    <div class="chat-messages" id="chatMessages"></div>
    <div class="chat-input">
      <input type="text" id="messageInput" placeholder="Digite uma mensagem..." />
      <button onclick="enviarMensagem()">Enviar</button>
    </div>
  `;
  
  //carregarMensagens(usuario.id);
  
  document.getElementById("messageInput").focus();
  
  document.getElementById("messageInput").addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      enviarMensagem();
    }
  });
}