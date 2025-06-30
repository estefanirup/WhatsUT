package server.rmi;

import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.*;

import server.model.Message;

public class ChatGrupoImpl extends UnicastRemoteObject implements ChatGrupoInterface {
    private static final String BASE_PATH = "server/data/grupos/";

    public ChatGrupoImpl() throws RemoteException {
        super();
        File dir = new File(BASE_PATH);
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public synchronized void enviarMensagemGrupo(String grupo, Message msg) throws RemoteException {
        File file = new File(BASE_PATH + grupo + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(msg.getId() + ";" + msg.getUserId() + ";" + msg.getHorario().getTime() + ";" + msg.getTexto().replace("\n", "\\n"));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Erro ao gravar mensagem de grupo");
        }
    }

    @Override
    public synchronized List<Message> listarMensagensGrupo(String grupo) throws RemoteException {
        List<Message> mensagens = new ArrayList<>();
        File file = new File(BASE_PATH + grupo + ".txt");

        if (!file.exists()) return mensagens;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 4);
                if (partes.length == 4) {
                    int id = Integer.parseInt(partes[0]);
                    int userId = Integer.parseInt(partes[1]);
                    long tempo = Long.parseLong(partes[2]);
                    String texto = partes[3].replace("\\n", "\n");

                    Message msg = new Message(id, userId, -1, texto);
                    msg.setHorario(new Date(tempo));
                    mensagens.add(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mensagens;
    }
}
