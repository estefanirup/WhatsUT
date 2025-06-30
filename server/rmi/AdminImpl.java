package server.rmi;

import server.model.BanRequest;
import server.model.UsuarioPublico; // Para obter o nome do usuário banido
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AdminImpl extends UnicastRemoteObject implements AdminInterface {
    private final String BAN_REQUESTS_FILE = "server/data/ban_requests.txt";
    private final String BANNED_USERS_FILE = "server/data/banned_users.txt"; // Novo arquivo para usuários banidos
    private int nextRequestId;

    // Map para armazenar usuários banidos em memória (ID do usuário -> true)
    private ConcurrentHashMap<Integer, Boolean> bannedUsersCache;

    // Referência para AuthImpl para manipular o status de login/visibilidade
    private AuthImpl authService;
    private UsuarioImpl usuarioService; // Para obter nomes de usuários

    // IDs de usuários administradores (hardcoded para simplicidade, em um sistema real viria de DB)
    private static final List<Integer> ADMIN_USER_IDS = List.of(1); // Exemplo: Usuário com ID 1 é admin

    public AdminImpl(AuthImpl authService, UsuarioImpl usuarioService) throws RemoteException {
        super();
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.nextRequestId = calculateNextRequestId();
        this.bannedUsersCache = new ConcurrentHashMap<>();
        loadBannedUsers(); // Carrega usuários banidos ao iniciar
    }

    private synchronized int calculateNextRequestId() {
        int maxId = 0;
        File file = new File(BAN_REQUESTS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";", -1);
                    if (parts.length >= 1) {
                        try {
                            int id = Integer.parseInt(parts[0]);
                            if (id > maxId) {
                                maxId = id;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Erro ao parsear ID da solicitação de banimento: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return maxId + 1;
    }

    private synchronized void loadBannedUsers() {
        File file = new File(BANNED_USERS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        int userId = Integer.parseInt(line.trim());
                        bannedUsersCache.put(userId, true);
                        // Também desloga o usuário se ele estiver online e banido
                        authService.logout(userId);
                        // E remove da lista de usuários visíveis para outros
                        usuarioService.removeUsuarioPublico(userId);
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao parsear ID de usuário banido: " + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void saveBannedUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BANNED_USERS_FILE))) {
            for (int userId : bannedUsersCache.keySet()) {
                writer.write(String.valueOf(userId));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int submitBanRequest(int requesterUserId, int targetUserId, String reason) throws RemoteException {
        if (requesterUserId == targetUserId) {
            throw new RemoteException("Não é possível solicitar o próprio banimento.");
        }
        // Verificar se o usuário já está banido
        if (bannedUsersCache.containsKey(targetUserId)) {
            throw new RemoteException("O usuário alvo já está banido.");
        }
        // Verificar se já existe uma solicitação PENDING para este alvo
        List<BanRequest> existingRequests = getAllBanRequests().stream()
                .filter(req -> req.getTargetUserId() == targetUserId && req.getStatus().equals("PENDING"))
                .collect(Collectors.toList());
        if (!existingRequests.isEmpty()) {
            throw new RemoteException("Já existe uma solicitação de banimento pendente para este usuário.");
        }

        BanRequest request = new BanRequest(nextRequestId++, requesterUserId, targetUserId, reason);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BAN_REQUESTS_FILE, true))) {
            // Formato: id;requesterId;targetId;reason;requestDate(timestamp);status;adminId;processDate(timestamp)
            String line = String.format("%d;%d;%d;%s;%d;%s;-1;-1",
                    request.getId(), request.getRequesterUserId(), request.getTargetUserId(),
                    request.getReason().replace("\n", "\\n"), // Escapa quebras de linha
                    request.getRequestDate().getTime(), request.getStatus());
            writer.write(line);
            writer.newLine();
            System.out.println("Solicitação de banimento submetida: " + request.getId());
            return request.getId();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao submeter solicitação de banimento.");
        }
    }

    private List<BanRequest> readAllBanRequestsFromFile() {
        List<BanRequest> requests = new ArrayList<>();
        File file = new File(BAN_REQUESTS_FILE);
        if (!file.exists()) return requests;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", -1);
                if (parts.length >= 6) { // Mínimo de campos para uma solicitação
                    try {
                        int id = Integer.parseInt(parts[0]);
                        int requesterId = Integer.parseInt(parts[1]);
                        int targetId = Integer.parseInt(parts[2]);
                        String reason = parts[3].replace("\\n", "\n");
                        long requestTimestamp = Long.parseLong(parts[4]);
                        String status = parts[5];

                        BanRequest request = new BanRequest(id, requesterId, targetId, reason);
                        request.setStatus(status);
                        request.setProcessDate(new Date(requestTimestamp)); // Usado para requestDate no construtor
                        
                        // Recarrega os campos de processamento se existirem
                        if (parts.length >= 8) {
                            int adminId = Integer.parseInt(parts[6]);
                            long processTimestamp = Long.parseLong(parts[7]);
                            if (adminId != -1) { // -1 significa não processado
                                request.setAdminId(adminId);
                                request.setProcessDate(new Date(processTimestamp));
                            }
                        }
                        requests.add(request);
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao parsear linha da solicitação de banimento: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requests;
    }

    private synchronized void writeAllBanRequestsToFile(List<BanRequest> requests) throws RemoteException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BAN_REQUESTS_FILE, false))) { // false para sobrescrever
            for (BanRequest request : requests) {
                String line = String.format("%d;%d;%d;%s;%d;%s;%d;%d",
                        request.getId(), request.getRequesterUserId(), request.getTargetUserId(),
                        request.getReason().replace("\n", "\\n"),
                        request.getRequestDate().getTime(), request.getStatus(),
                        request.getAdminId(),
                        request.getProcessDate() != null ? request.getProcessDate().getTime() : -1);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao salvar solicitações de banimento.");
        }
    }

    @Override
    public List<BanRequest> getAllBanRequests() throws RemoteException {
        return readAllBanRequestsFromFile();
    }

    @Override
    public List<BanRequest> getPendingBanRequests() throws RemoteException {
        return readAllBanRequestsFromFile().stream()
                .filter(req -> req.getStatus().equals("PENDING"))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized boolean processBanRequest(int requestId, int adminId, String status) throws RemoteException {
        if (!isAdmin(adminId)) {
            throw new RemoteException("Apenas administradores podem processar solicitações de banimento.");
        }

        List<BanRequest> requests = readAllBanRequestsFromFile();
        boolean found = false;
        for (BanRequest req : requests) {
            if (req.getId() == requestId && req.getStatus().equals("PENDING")) {
                req.setStatus(status);
                req.setAdminId(adminId);
                req.setProcessDate(new Date());
                found = true;

                if (status.equals("APPROVED")) {
                    // Chamar método para banir o usuário
                    banUser(adminId, req.getTargetUserId());
                    System.out.println("Solicitação de banimento APROVADA para o usuário ID: " + req.getTargetUserId());
                } else {
                    System.out.println("Solicitação de banimento REJEITADA para o usuário ID: " + req.getTargetUserId());
                }
                break;
            }
        }
        if (found) {
            writeAllBanRequestsToFile(requests);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean banUser(int adminId, int targetUserId) throws RemoteException {
        if (!isAdmin(adminId)) {
            throw new RemoteException("Apenas administradores podem banir usuários.");
        }
        if (bannedUsersCache.containsKey(targetUserId)) {
            return false; // Já banido
        }
        bannedUsersCache.put(targetUserId, true);
        saveBannedUsers();
        // Deslogar o usuário e remover da lista de visíveis
        authService.logout(targetUserId);
        usuarioService.removeUsuarioPublico(targetUserId);
        System.out.println("Usuário ID " + targetUserId + " foi banido.");
        return true;
    }

    @Override
    public synchronized boolean unbanUser(int adminId, int targetUserId) throws RemoteException {
        if (!isAdmin(adminId)) {
            throw new RemoteException("Apenas administradores podem desbanir usuários.");
        }
        if (!bannedUsersCache.containsKey(targetUserId)) {
            return false; // Não está banido
        }
        bannedUsersCache.remove(targetUserId);
        saveBannedUsers();
        // Re-adicionar à lista de visíveis se ele não estiver online (se estivesse, seria adicionado no login)
        // Isso é um pouco mais complexo, pois `addUsuarioPublico` precisa do nome e imagem.
        // Por simplicidade, assumimos que ele aparecerá novamente quando logar.
        System.out.println("Usuário ID " + targetUserId + " foi desbanido.");
        return true;
    }

    @Override
    public boolean isAdmin(int userId) throws RemoteException {
        return ADMIN_USER_IDS.contains(userId);
    }
}