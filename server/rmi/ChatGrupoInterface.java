package server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import server.model.Message;

public interface ChatGrupoInterface extends Remote {
    void enviarMensagemGrupo(int grupoId, Message msg) throws RemoteException;
    List<Message> listarMensagensGrupo(int grupoId) throws RemoteException;
    int getNextMessageId() throws RemoteException;
}
