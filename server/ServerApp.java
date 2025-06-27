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
            AuthInterface auth = new AuthImpl();
            UsuarioInterface usuario = new UsuarioImpl();

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("AuthService", auth);
            registry.rebind("UsuarioService", usuario);

            System.out.println("Servidor RMI pronto...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
