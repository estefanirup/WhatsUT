package client;

import static spark.Spark.*;

import com.google.gson.Gson;
import server.rmi.AuthInterface;
import server.rmi.UsuarioInterface;
import server.model.UsuarioPublico;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;

public class HttpBridge {
    public static void main(String[] args) {
        port(4567); // Spark ouvindo na porta 4567
        Gson gson = new Gson();

        final AuthInterface[] authHolder = new AuthInterface[1];
        final UsuarioInterface[] usuarioHolder = new UsuarioInterface[1];

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            authHolder[0] = (AuthInterface) registry.lookup("AuthService");
            usuarioHolder[0] = (UsuarioInterface) registry.lookup("UsuarioService");
            System.out.println("✅ Conectado ao servidor RMI.");
        } catch (Exception e) {
            System.out.println("❌ Não foi possível conectar ao RMI:");
            e.printStackTrace();
        }

        // Habilita CORS
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) response.header("Access-Control-Allow-Headers", headers);

            String methods = request.headers("Access-Control-Request-Method");
            if (methods != null) response.header("Access-Control-Allow-Methods", methods);

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        });

        // Rota de teste
        get("/teste", (req, res) -> "Servidor HTTP do WhatsUT rodando! 🚀");

        // Endpoint de login
        post("/login", (req, res) -> {
            res.type("application/json");
            LoginRequest data = gson.fromJson(req.body(), LoginRequest.class);

            boolean sucesso = false;
            if (authHolder[0] != null) {
                sucesso = authHolder[0].login(data.usuario, data.senha);
            }

            return gson.toJson(new LoginResponse(sucesso));
        });

        // ✅ Novo endpoint: retorna lista de usuários visíveis
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
    }

    // Classe auxiliar para JSON de login
    static class LoginRequest {
        String usuario;
        String senha;
    }

    static class LoginResponse {
        boolean sucesso;
        LoginResponse(boolean sucesso) {
            this.sucesso = sucesso;
        }
    }
}
