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
                    String[] partes = linha.split(";");
                    if (partes.length >= 1) {
                        int id = Integer.parseInt(partes[0]);
                        if (id > maxId) {
                            maxId = id;
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
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

    private void salvarMensagem(Message message, boolean isGroup) throws RemoteException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(String.format("%d;%d;%d;%d;%s;%s",
                message.getId(),
                message.getUserId(),
                message.getDestinatarioId(),
                message.getHorario().getTime(),
                message.getTexto().replace("\n", "\\n"),
                isGroup ? "G" : "U"
            ));
            writer.newLine();
            System.out.println("Gravando mensagem " + (isGroup ? "de grupo" : "privada") + ": " + message.getTexto());
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

    private List<Message> filtrarMensagens(int remetenteId, int destinoId, boolean isGroup) {
        List<Message> result = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return result;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 6);
                if (partes.length == 6) {
                    int id = Integer.parseInt(partes[0]);
                    int remetente = Integer.parseInt(partes[1]);
                    int destinatario = Integer.parseInt(partes[2]);
                    long timestamp = Long.parseLong(partes[3]);
                    String texto = partes[4].replace("\\n", "\n");
                    boolean grupo = "G".equals(partes[5]);

                    if (grupo == isGroup) {
                        if (!grupo) {
                            // Conversa privada (duas direções)
                            if ((remetente == remetenteId && destinatario == destinoId) ||
                                (remetente == destinoId && destinatario == remetenteId)) {
                                Message msg = new Message(id, remetente, destinatario, texto);
                                msg.setHorario(new Date(timestamp));
                                result.add(msg);
                            }
                        } else {
                            // Mensagens de grupo (destino fixo)
                            if (destinatario == destinoId) {
                                Message msg = new Message(id, remetente, destinatario, texto);
                                msg.setHorario(new Date(timestamp));
                                result.add(msg);
                            }
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
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
