package server.rmi;

import server.model.UsuarioPublico;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface UsuarioInterface extends Remote {
    List<UsuarioPublico> listarUsuarios() throws RemoteException;
}