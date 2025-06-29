async function fazerLogin() {
  const user = document.getElementById("usuario").value;
  const senha = document.getElementById("senha").value;

  if (!user || !senha) {
    alert("Por favor, preencha usuário e senha.");
    return;
  }

  try {
    const response = await fetch("http://localhost:4567/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usuario: user, senha: senha }),
    });

    if (!response.ok) {
      alert("Erro ao conectar com o servidor.");
      return;
    }

    const resultado = await response.json();

    if (resultado.sucesso) {
      sessionStorage.setItem('loggedUserId', resultado.userId);
      sessionStorage.setItem('username', user);
      alert("Login realizado com sucesso!");
      window.location.href = "users.html";
    } else {
      alert("Usuário ou senha inválidos.");
    }
  } catch (error) {
    alert("Erro ao conectar com o servidor.");
    console.error(error);
  }
}

document.addEventListener('DOMContentLoaded', function() {
  // codigo de inicializacao
});