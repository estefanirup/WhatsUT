package server;

import server.rmi.AuthImpl;
import server.rmi.AuthInterface;
import server.rmi.UsuarioImpl;
import server.rmi.UsuarioInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerApp {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            AuthInterface auth = new AuthImpl();
            registry.rebind("AuthService", auth);

            UsuarioInterface usuarioService = new UsuarioImpl();
            registry.rebind("UsuarioService", usuarioService);

            System.out.println("✅ Servidor RMI pronto e rodando na porta 1099...");
        } catch (Exception e) {
            System.out.println("❌ Erro ao iniciar o servidor RMI:");
            e.printStackTrace();
        }
    }
}
