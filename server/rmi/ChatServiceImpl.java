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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(String.format("%d;%d;%d;%d;%s", message.getId(), message.getUserId(), message.getDestinatarioId(), message.getHorario().getTime(), message.getTexto().replace("\n", "\\n")));
            writer.newLine();
            System.out.println("Gravando mensagem: " + message.getTexto());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao salvar mensagem.");
        }
    }

    @Override
    public synchronized List<Message> getMessages(int userId, int destinatarioId) throws RemoteException {
        List<Message> result = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return result;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 5);
                if (partes.length == 5) {
                    int id = Integer.parseInt(partes[0]);
                    int remetente = Integer.parseInt(partes[1]);
                    int destinatario = Integer.parseInt(partes[2]);
                    long timestamp = Long.parseLong(partes[3]);
                    String texto = partes[4].replace("\\n", "\n");

                    // Conversa entre dois usuários (em qualquer direção)
                    if ((remetente == userId && destinatario == destinatarioId) ||
                        (remetente == destinatarioId && destinatario == userId)) {
                        Message message = new Message(id, remetente, destinatario, texto);
                        message.setHorario(new Date(timestamp));
                        result.add(message);
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
