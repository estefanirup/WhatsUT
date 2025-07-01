package server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import server.model.Grupo;

public interface GrupoInterface extends Remote {
    boolean criarGrupo(int id, int idAdmin, String nome, String descricao, String criador, List<Integer> membros) throws RemoteException;
    List<Integer> listarGrupos() throws RemoteException;
    public List<Grupo> listarGruposComDetalhes() throws RemoteException;
    boolean entrarNoGrupo(int grupoId, int usuarioId) throws RemoteException;
    List<Integer> listarMembros(int grupoId) throws RemoteException;
    List<Grupo> listarGruposDoUsuario(int usuarioId) throws RemoteException;
    boolean sairDoGrupo(int grupoId, int usuarioId) throws RemoteException;
    boolean ehAdmin(int grupoId, int usuarioId) throws RemoteException;
    boolean ehParticipante(int grupoId, int usuarioId) throws RemoteException;
    boolean banirUsuario(int grupoId, int adminId, int usuarioParaBanir) throws RemoteException;

    // Methods for managing join requests
    Set<Integer> listarPedidosPendentes(int grupoId, int adminId) throws RemoteException;
    boolean aprovarEntrada(int grupoId, int adminId, int usuarioId) throws RemoteException;
    boolean rejeitarEntrada(int grupoId, int adminId, int usuarioId) throws RemoteException;
    
    // Additional helper methods
    String getNomeGrupo(int grupoId) throws RemoteException;
    int getIdAdmin(int grupoId) throws RemoteException;
}