package server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import server.model.Message;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private final String FILE_PATH = "server/data/mensagens.txt";
    private int nextId;

    public ChatServiceImpl() throws RemoteException {
        super();
        this.nextId = calcularProximoId();
    }

    private synchronized int calcularProximoId() {
        int maxId = 0;
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    String[] partes = linha.split(";", -1); // Usar -1 para garantir partes vazias
                    if (partes.length >= 1) {
                        try {
                            int id = Integer.parseInt(partes[0]);
                            if (id > maxId) {
                                maxId = id;
                            }
                        } catch (NumberFormatException e) {
                            // Ignorar linhas mal formatadas, ou logar para depuração
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return maxId + 1;
    }

    @Override
    public synchronized void sendMessage(Message message) throws RemoteException {
        salvarMensagem(message, false);
    }

    @Override
    public synchronized void sendGroupMessage(Message message) throws RemoteException {
        salvarMensagem(message, true);
    }

    @Override
    public synchronized void sendFileMessage(Message message) throws RemoteException {
        salvarMensagem(message, false); // Para conversas privadas
        // Se for possível enviar arquivo em grupo, adicione uma lógica similar com um flag diferente
    }

    private void salvarMensagem(Message message, boolean isGroup) throws RemoteException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            // Novo formato: id;userId;destinatarioId;horario;texto;tipo;fileName;fileMimeType;fileContentBase64
            // O tipo pode ser "U" para mensagem de usuário, "G" para grupo, "F" para arquivo.
            // Para simplicidade, vamos usar "UM" para UserMessage e "FM" para FileMessage.

            String tipoMensagem;
            if (message.getFileContentBase64() != null && !message.getFileContentBase64().isEmpty()) {
                tipoMensagem = "FM"; // File Message
            } else if (isGroup) {
                tipoMensagem = "G"; // Group Message
            } else {
                tipoMensagem = "U"; // User Message
            }

            String linha = String.format("%d;%d;%d;%d;%s;%s;%s;%s;%s",
                message.getId(),
                message.getUserId(),
                message.getDestinatarioId(),
                message.getHorario().getTime(),
                message.getTexto().replace("\n", "\\n"), // Escapa quebras de linha
                tipoMensagem,
                message.getFileName() != null ? message.getFileName() : "", // Garante que não é nulo
                message.getFileMimeType() != null ? message.getFileMimeType() : "", // Garante que não é nulo
                message.getFileContentBase64() != null ? message.getFileContentBase64() : "" // Garante que não é nulo
            );
            writer.write(linha);
            writer.newLine();
            System.out.println("Gravando mensagem " + tipoMensagem + ": " + message.getTexto() + (message.getFileName() != null ? " (Arquivo: " + message.getFileName() + ")" : ""));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao salvar mensagem.");
        }
    }

    @Override
    public synchronized List<Message> getMessages(int userId, int destinatarioId) throws RemoteException {
        return filtrarMensagens(userId, destinatarioId, false);
    }

    @Override
    public synchronized List<Message> getGroupMessages(int grupoId) throws RemoteException {
        return filtrarMensagens(-1, grupoId, true);
    }

    private List<Message> filtrarMensagens(int remetenteId, int destinoId, boolean isGroupFilter) {
        List<Message> result = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return result;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", -1); // Usar -1 para garantir partes vazias
                if (partes.length >= 6) { // Mínimo de campos para mensagem de texto ou arquivo
                    try {
                        int id = Integer.parseInt(partes[0]);
                        int remetente = Integer.parseInt(partes[1]);
                        int destinatario = Integer.parseInt(partes[2]);
                        long timestamp = Long.parseLong(partes[3]);
                        String texto = partes[4].replace("\\n", "\n");
                        String tipoMensagem = partes[5]; // "U", "G", "FM"

                        String fileName = (partes.length > 6 && !partes[6].isEmpty()) ? partes[6] : null;
                        String fileMimeType = (partes.length > 7 && !partes[7].isEmpty()) ? partes[7] : null;
                        String fileContentBase64 = (partes.length > 8 && !partes[8].isEmpty()) ? partes[8] : null;

                        boolean isFileMessage = "FM".equals(tipoMensagem);
                        boolean isGroupMessage = "G".equals(tipoMensagem);
                        boolean isTextMessage = "U".equals(tipoMensagem);

                        // Lógica de filtragem
                        if (isGroupFilter) { // Filtrando por grupo
                            if (isGroupMessage && destinatario == destinoId) {
                                Message msg = new Message(id, remetente, destinatario, texto);
                                msg.setHorario(new Date(timestamp));
                                result.add(msg);
                            }
                            // Se for para permitir arquivos em grupo, adicione aqui:
                            // else if (isFileMessage && destinatario == destinoId && isGroupMessageForFile) {
                            //    Message msg = new Message(id, remetente, destinatario, texto, fileName, fileMimeType, fileContentBase64);
                            //    msg.setHorario(new Date(timestamp));
                            //    result.add(msg);
                            // }
                        } else { // Filtrando por conversa privada (entre dois usuários)
                            if (isFileMessage || isTextMessage) { // Considera mensagens de texto e arquivo
                                if ((remetente == remetenteId && destinatario == destinoId) ||
                                    (remetente == destinoId && destinatario == remetenteId)) {
                                    Message msg;
                                    if (isFileMessage) {
                                        msg = new Message(id, remetente, destinatario, texto, fileName, fileMimeType, fileContentBase64);
                                    } else { // isTextMessage
                                        msg = new Message(id, remetente, destinatario, texto);
                                    }
                                    msg.setHorario(new Date(timestamp));
                                    result.add(msg);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace(); // Logar erro na linha
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        result.sort(Comparator.comparing(Message::getHorario));
        return result;
    }

    @Override
    public synchronized int getNextMessageId() throws RemoteException {
        return nextId++;
    }
}