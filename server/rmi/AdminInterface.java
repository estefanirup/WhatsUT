package server.rmi;

import server.model.BanRequest;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AdminInterface extends Remote {
    // Para um usuário comum solicitar um banimento
    int submitBanRequest(int requesterUserId, int targetUserId, String reason) throws RemoteException;

    // Para o administrador listar todas as solicitações (ou apenas pendentes)
    List<BanRequest> getAllBanRequests() throws RemoteException;
    List<BanRequest> getPendingBanRequests() throws RemoteException;

    // Para o administrador aprovar ou rejeitar uma solicitação
    boolean processBanRequest(int requestId, int adminId, String status) throws RemoteException; // status: "APPROVED" ou "REJECTED"

    // Método para o administrador banir/desbanir um usuário diretamente
    boolean banUser(int adminId, int targetUserId) throws RemoteException;
    boolean unbanUser(int adminId, int targetUserId) throws RemoteException;

    // Método para verificar se um usuário é administrador
    boolean isAdmin(int userId) throws RemoteException;
}