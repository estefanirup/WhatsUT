package client;

import static spark.Spark.*;

import com.google.gson.Gson;
import server.rmi.AuthInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HttpBridge {
    public static void main(String[] args) {
        port(4567); // Spark ouvindo na porta 4567
        Gson gson = new Gson();

        // Truque: usamos um array final para contornar a limitação do Java com lambdas
        final AuthInterface[] authHolder = new AuthInterface[1];

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            authHolder[0] = (AuthInterface) registry.lookup("AuthService");
            System.out.println("✅ Conectado ao servidor RMI.");
        } catch (Exception e) {
            System.out.println("❌ Não foi possível conectar ao RMI:");
            e.printStackTrace();
        }

        // Endpoint simples de teste
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
    }

    // Classe para receber JSON do login
    static class LoginRequest {
        String usuario;
        String senha;
    }

    // Classe para responder ao cliente
    static class LoginResponse {
        boolean sucesso;

        LoginResponse(boolean sucesso) {
            this.sucesso = sucesso;
        }
    }
}
