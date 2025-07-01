package server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import server.model.Message;

public interface ChatService extends Remote {
    void sendMessage(Message message) throws RemoteException;

    List<Message> getMessages(int userId, int destinatarioId) throws RemoteException;

    // List<Message> getNewMessages(int userId, int mensagemId) throws RemoteException;
    int getNextMessageId() throws RemoteException;

    List<Message> getGroupMessages(int grupoId) throws RemoteException;

    void sendGroupMessage(Message message) throws RemoteException;

}