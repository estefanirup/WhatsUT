package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.rmi.AuthImpl;
import server.rmi.AuthInterface;
import server.rmi.ChatGrupoImpl;
import server.rmi.ChatGrupoInterface;
import server.rmi.ChatServiceImpl;
import server.rmi.GrupoImpl;
import server.rmi.GrupoInterface;
import server.rmi.UsuarioImpl;
import server.rmi.UsuarioInterface;


public class ServerApp {
    public static void main(String[] args) {
        try {
            AuthInterface auth = new AuthImpl();
            UsuarioInterface usuario = new UsuarioImpl();
            ChatServiceImpl mensagem = new ChatServiceImpl();
            ChatGrupoInterface chatGrupo = new ChatGrupoImpl();
            GrupoInterface grupoService = new GrupoImpl();

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("AuthService", auth);
            registry.rebind("UsuarioService", usuario);
            registry.rebind("ChatService", mensagem);
            registry.rebind("GrupoService", grupoService);
            registry.rebind("ChatGrupoService", chatGrupo);

            System.out.println("Servidor RMI pronto...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
