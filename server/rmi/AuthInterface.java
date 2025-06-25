package server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuthInterface extends Remote {
    boolean registrar(String nome, String senha) throws RemoteException;
    boolean login(String nome, String senha) throws RemoteException;
    List<String> listarUsuarios() throws RemoteException;
}
