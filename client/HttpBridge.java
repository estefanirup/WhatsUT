package client;

import static spark.Spark.*;

import com.google.gson.Gson;
import server.rmi.AuthInterface;
import server.rmi.UsuarioInterface;
import server.rmi.ChatService;
import server.model.UsuarioPublico;
import server.model.Message;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;

public class HttpBridge {

    public static void main(String[] args) {
        port(4567);
        Gson gson = new Gson();

        final AuthInterface[] authHolder = new AuthInterface[1];
        final UsuarioInterface[] usuarioHolder = new UsuarioInterface[1];
        final ChatService[] chatHolder = new ChatService[1];

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            authHolder[0] = (AuthInterface) registry.lookup("AuthService");
            usuarioHolder[0] = (UsuarioInterface) registry.lookup("UsuarioService");
            chatHolder[0] = (ChatService) registry.lookup("ChatService");
            System.out.println("✅ Conectado ao servidor RMI.");
        } catch (Exception e) {
            System.out.println("❌ Não foi possível conectar ao RMI:");
            e.printStackTrace();
        }

        // CORS
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) {
                response.header("Access-Control-Allow-Headers", headers);
            }

            String methods = request.headers("Access-Control-Request-Method");
            if (methods != null) {
                response.header("Access-Control-Allow-Methods", methods);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        });

        // Teste simples
        get("/teste", (req, res) -> "Servidor HTTP do WhatsUT rodando! 🚀");

        // Login
        post("/login", (req, res) -> {
            res.type("application/json");
            LoginRequest data = gson.fromJson(req.body(), LoginRequest.class);

            boolean sucesso = false;
            int userId = -1;
            if (authHolder[0] != null) {
                sucesso = authHolder[0].login(data.usuario, data.senha);
                if (sucesso) {
                    userId = authHolder[0].getUserId(data.usuario);
                }
            }

            return gson.toJson(new LoginResponse(sucesso, userId));
        });

        // Registro
        post("/register", (req, res) -> {
            res.type("application/json");
            try {
                RegisterRequest data = gson.fromJson(req.body(), RegisterRequest.class);
                System.out.println("Tentando registrar: " + data.usuario);

                boolean sucesso = false;
                String erro = null;

                if (authHolder[0] != null) {
                    sucesso = authHolder[0].registrar(data.usuario, data.senha);
                    if (!sucesso) {
                        erro = "Usuário já existe";
                    }
                } else {
                    erro = "Serviço de autenticação indisponível";
                }

                return gson.toJson(new RegisterResponse(sucesso, erro));
            } catch (Exception e) {
                e.printStackTrace();
                return gson.toJson(new RegisterResponse(false, "Erro interno: " + e.getMessage()));
            }
        });

        // Lista de usuários visíveis (via UsuarioService)
        get("/api/usuarios", (req, res) -> {
            res.type("application/json");
            if (usuarioHolder[0] != null) {
                try {
                    List<UsuarioPublico> lista = usuarioHolder[0].listarUsuarios();
                    return gson.toJson(lista);
                } catch (Exception e) {
                    e.printStackTrace();
                    return gson.toJson(Collections.emptyList());
                }
            } else {
                return gson.toJson(Collections.emptyList());
            }
        });

    // Mensagens
        post("/api/messages/send", (req, res) -> {
            res.type("application/json");
            try {
                SendMessageRequest data = gson.fromJson(req.body(), SendMessageRequest.class);
                if (chatHolder[0] != null) {
                    Message message = new Message(
                        chatHolder[0].getNextMessageId(),
                        data.remetenteId,
                        data.destinatarioId,
                        data.texto
                    );
                    chatHolder[0].sendMessage(message);
                    return gson.toJson(new SendMessageResponse(true, message.getId()));
                }
                else {
                    return gson.toJson(new SendMessageResponse(false, -1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return gson.toJson(new SendMessageResponse(false, -1));
            }
        });

        get("/api/messages/:userId/:destinatarioId", (req, res) -> {
            res.type("application/json");
            try {
                int userId = Integer.parseInt(req.params("userId"));
                int destinatarioId = Integer.parseInt(req.params("destinatarioId"));
                if (chatHolder[0] != null) {
                    List<Message> messages = chatHolder[0].getMessages(userId, destinatarioId);
                    return gson.toJson(messages);
                }
                return gson.toJson(Collections.emptyList());
            } catch (Exception e) {
                e.printStackTrace();
                return gson.toJson(Collections.emptyList());
            }
        });
    }

    static class LoginRequest {

        String usuario;
        String senha;
    }

    static class LoginResponse {

        boolean sucesso;
        int userId;

        LoginResponse(boolean sucesso, int userId) {
            this.sucesso = sucesso;
            this.userId = userId;
        }
    }

    public static class RegisterRequest {

        String usuario;
        String senha;
    }

    public static class RegisterResponse {

        boolean sucesso;
        String erro;

        public RegisterResponse(boolean sucesso, String erro) {
            this.sucesso = sucesso;
            this.erro = erro;
        }
    }

    static class SendMessageRequest {

        int remetenteId;
        int destinatarioId;
        String texto;
    }

    static class SendMessageResponse {

        boolean success;
        int messageId;

        public SendMessageResponse(boolean success, int messageId) {
            this.success = success;
            this.messageId = messageId;
        }
    }

}
