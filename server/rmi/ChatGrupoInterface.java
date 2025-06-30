package server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import server.model.Message;

public interface ChatGrupoInterface extends Remote {
    void enviarMensagemGrupo(String grupo, Message msg) throws RemoteException;
    List<Message> listarMensagensGrupo(String grupo) throws RemoteException;
}
