package server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GrupoInterface extends Remote {
    boolean criarGrupo(String nomeGrupo, String criador) throws RemoteException;
    List<String> listarGrupos() throws RemoteException;
    boolean entrarNoGrupo(String nomeGrupo, String usuario) throws RemoteException;
    List<String> listarMembros(String nomeGrupo) throws RemoteException;
    boolean sairDoGrupo(String nomeGrupo, String usuario) throws RemoteException;
    boolean ehAdmin(String nomeGrupo, String usuario) throws RemoteException;
    boolean banirUsuario(String nomeGrupo, String admin, String usuarioParaBanir) throws RemoteException;
}
