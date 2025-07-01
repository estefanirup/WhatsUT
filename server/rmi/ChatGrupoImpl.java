package server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import server.model.Message;

public class ChatGrupoImpl extends UnicastRemoteObject implements ChatGrupoInterface {
    private static final String BASE_PATH = "server/data/mensagens.txt";
    private int messageCounter;

    public ChatGrupoImpl() throws RemoteException {
        super();
        this.messageCounter = calcularProximoId();
        File file = new File(BASE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Erro ao criar arquivo de mensagens: " + e.getMessage());
            }
        }
    }

    private synchronized int calcularProximoId() {
        int maxId = 0;
        File file = new File(BASE_PATH);
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
    public synchronized int getNextMessageId() throws RemoteException {
        return ++messageCounter;
    }

    @Override
    public synchronized void enviarMensagemGrupo(int grupoId, Message msg) throws RemoteException {
        try {
            //msg.setDestinatarioId(grupoId);
            salvarMensagem(msg, true);
            System.out.println("Mensagem enviada para o grupo " + grupoId + ": " + msg.getTexto());
        } catch (RemoteException e) {
            System.err.println("Erro ao enviar mensagem para grupo " + grupoId + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public synchronized List<Message> listarMensagensGrupo(int grupoId) throws RemoteException {
        List<Message> mensagens = new ArrayList<>();
        File file = new File(BASE_PATH);

        if (!file.exists()) return mensagens;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] partes = linha.split(";", 6);
                if (partes.length == 6 && "G".equals(partes[5])) {
                    try {
                        int id = Integer.parseInt(partes[0]);
                        int userId = Integer.parseInt(partes[1]);
                        int destinatarioId = Integer.parseInt(partes[2]);
                        long tempo = Long.parseLong(partes[3]);
                        String texto = partes[4].replace("\\n", "\n");

                        if (destinatarioId == grupoId) {
                            Message msg = new Message(id, userId, destinatarioId, texto);
                            msg.setHorario(new Date(tempo));
                            mensagens.add(msg);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Formato inválido na mensagem: " + linha);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler mensagens do grupo " + grupoId + ": " + e.getMessage());
            throw new RemoteException("Erro ao recuperar mensagens do grupo");
        }

        return mensagens;
    }

    private synchronized void salvarMensagem(Message message, boolean isGroup) throws RemoteException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_PATH, true))) {
            //writer.newLine();
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
            System.err.println("Erro ao salvar mensagem: " + e.getMessage());
            throw new RemoteException("Erro ao salvar mensagem.");
        }
    }
}