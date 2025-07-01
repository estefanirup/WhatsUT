package client;

import static spark.Spark.*;

import com.google.gson.Gson;
import server.rmi.AuthInterface;
import server.rmi.UsuarioInterface;
import server.rmi.ChatService;
import server.rmi.ChatGrupoInterface;
import server.rmi.GrupoInterface;
import server.model.UsuarioPublico;
import server.model.Message;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Date;
import server.model.Grupo;

public class HttpBridge {

    public static void main(String[] args) {
        port(4567);
        Gson gson = new Gson();

        final AuthInterface[] authHolder = new AuthInterface[1];
        final UsuarioInterface[] usuarioHolder = new UsuarioInterface[1];
        final ChatService[] chatHolder = new ChatService[1];
        final ChatGrupoInterface[] chatGrupoHolder = new ChatGrupoInterface[1];
        final GrupoInterface[] grupoHolder = new GrupoInterface[1];

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            authHolder[0] = (AuthInterface) registry.lookup("AuthService");
            usuarioHolder[0] = (UsuarioInterface) registry.lookup("UsuarioService");
            chatHolder[0] = (ChatService) registry.lookup("ChatService");
            chatGrupoHolder[0] = (ChatGrupoInterface) registry.lookup("ChatGrupoService");
            grupoHolder[0] = (GrupoInterface) registry.lookup("GrupoService");
            System.out.println("Conectado ao servidor RMI.");
        } catch (Exception e) {
            System.out.println("Não foi possível conectar ao RMI:");
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
                            data.texto);
                    chatHolder[0].sendMessage(message);
                    return gson.toJson(new SendMessageResponse(true, message.getId()));
                } else {
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

        // GRUPO
        post("/api/grupos", (req, res) -> {
            res.type("application/json");
            try {
                CreateGroupRequest data = gson.fromJson(req.body(), CreateGroupRequest.class);
                boolean success = grupoHolder[0] != null && grupoHolder[0].criarGrupo(
                        data.id,
                        data.idAdmin,
                        data.nome,
                        data.descricao,
                        data.criador,
                        data.membros);
                return gson.toJson(new BasicResponse(success));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        get("/api/grupos", (req, res) -> {
            res.type("application/json");
            try {
                List<Grupo> grupos = grupoHolder[0] != null
                        ? grupoHolder[0].listarGruposComDetalhes()
                        : Collections.emptyList();
                return gson.toJson(grupos);
            } catch (Exception e) {
                return gson.toJson(Collections.emptyList());
            }
        });

        post("/api/grupos/entrar", (req, res) -> {
            res.type("application/json");
            try {
                JoinGroupRequest data = gson.fromJson(req.body(), JoinGroupRequest.class);
                boolean success = grupoHolder[0] != null
                        && grupoHolder[0].entrarNoGrupo(data.grupoId, data.usuarioId);
                return gson.toJson(new BasicResponse(success));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        // List group members endpoint
        get("/api/grupos/:id/membros", (req, res) -> {
            res.type("application/json");
            try {
                int grupoId = Integer.parseInt(req.params("id"));
                List<Integer> membros = grupoHolder[0] != null
                        ? grupoHolder[0].listarMembros(grupoId)
                        : Collections.emptyList();
                return gson.toJson(membros);
            } catch (Exception e) {
                return gson.toJson(Collections.emptyList());
            }
        });

        // grupos a qual um usuario faz parte
        get("/api/grupos/:userId", (req, res) -> {
            res.type("application/json");
            try {
                int userId = Integer.parseInt(req.params("userId"));
                List<Grupo> grupos = grupoHolder[0] != null
                        ? grupoHolder[0].listarGruposDoUsuario(userId)
                        : Collections.emptyList();
                return gson.toJson(grupos);
            } catch (Exception e) {
                return gson.toJson(Collections.emptyList());
            }
        });

        get("/api/grupos/:id/pedidos", (req, res) -> {
            res.type("application/json");
            try {
                int grupoId = Integer.parseInt(req.params("id"));
                int adminId = Integer.parseInt(req.queryParams("adminId"));
                Set<Integer> pedidos = grupoHolder[0] != null
                        ? grupoHolder[0].listarPedidosPendentes(grupoId, adminId)
                        : Collections.emptySet();
                return gson.toJson(pedidos);
            } catch (Exception e) {
                return gson.toJson(Collections.emptySet());
            }
        });

        get("/api/grupos/:id/admin", (req, res) -> {
            res.type("application/json");
            try {
                int grupoId = Integer.parseInt(req.params("id"));
                int usuarioId = Integer.parseInt(req.queryParams("usuarioId"));

                boolean ehAdmin = grupoHolder[0] != null && grupoHolder[0].ehAdmin(grupoId, usuarioId);
                return gson.toJson(new BasicResponse(ehAdmin));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        post("/api/grupos/aprovar", (req, res) -> {
            res.type("application/json");
            try {
                ApproveRequest data = gson.fromJson(req.body(), ApproveRequest.class);
                boolean success = grupoHolder[0] != null
                        && grupoHolder[0].aprovarEntrada(data.grupoId, data.adminId, data.usuarioId);
                return gson.toJson(new BasicResponse(success));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        post("/api/grupos/rejeitar", (req, res) -> {
            res.type("application/json");
            try {
                ApproveRequest data = gson.fromJson(req.body(), ApproveRequest.class);
                boolean success = grupoHolder[0] != null &&
                        grupoHolder[0].rejeitarEntrada(data.grupoId, data.adminId, data.usuarioId);
                return gson.toJson(new BasicResponse(success));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        post("/api/grupos/banir", (req, res) -> {
            res.type("application/json");
            try {
                BanirRequest data = gson.fromJson(req.body(), BanirRequest.class);
                boolean success = grupoHolder[0] != null
                        && grupoHolder[0].banirUsuario(data.grupoId, data.adminId, data.usuarioParaBanir);
                return gson.toJson(new BasicResponse(success));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        // Group message endpoints
        post("/api/grupos/mensagens/enviar", (req, res) -> {
            res.type("application/json");
            try {
                SendGroupMessageRequest data = gson.fromJson(req.body(), SendGroupMessageRequest.class);
                if (chatGrupoHolder[0] != null) {
                    Message message = new Message(
                            chatGrupoHolder[0].getNextMessageId(),
                            data.remetenteId,
                            data.grupoId,
                            data.texto);
                    chatGrupoHolder[0].enviarMensagemGrupo(data.grupoId, message);
                    return gson.toJson(new BasicResponse(true));
                }
                return gson.toJson(new BasicResponse(false));
            } catch (Exception e) {
                return gson.toJson(new BasicResponse(false, e.getMessage()));
            }
        });

        get("/api/grupos/mensagens/:grupoId", (req, res) -> {
            res.type("application/json");
            try {
                int grupoId = Integer.parseInt(req.params("grupoId"));
                List<Message> messages = chatGrupoHolder[0] != null
                        ? chatGrupoHolder[0].listarMensagensGrupo(grupoId)
                        : Collections.emptyList();
                return gson.toJson(messages);
            } catch (Exception e) {
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

    static class CreateGroupRequest {

        int id;
        int idAdmin;
        String nome;
        String descricao;
        String criador;
        List<Integer> membros;
    }

    static class JoinGroupRequest {

        int grupoId;
        int usuarioId;
    }

    static class ApproveRequest {

        int grupoId;
        int adminId;
        int usuarioId;
    }

    static class BanirRequest {
        int grupoId;
        int adminId;
        int usuarioParaBanir;
    }

    static class SendGroupMessageRequest {

        int remetenteId;
        int grupoId;
        String texto;
    }

    static class BasicResponse {

        boolean success;
        String error;

        BasicResponse(boolean s) {
            success = s;
        }

        BasicResponse(boolean s, String e) {
            success = s;
            error = e;
        }
    }
}
