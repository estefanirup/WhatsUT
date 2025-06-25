package server;

import server.rmi.AuthImpl;
import server.rmi.AuthInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerApp {
    public static void main(String[] args) {
        try {
            AuthInterface auth = new AuthImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("AuthService", auth);
            System.out.println("Servidor RMI pronto...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
