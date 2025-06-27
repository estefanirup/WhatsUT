package server.rmi;

import server.model.UsuarioPublico;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class UsuarioImpl extends UnicastRemoteObject implements UsuarioInterface {

    private final String FILE_PATH = "server/data/usuarios_visiveis.txt";

    public UsuarioImpl() throws RemoteException {
        super();
    }

    @Override
    public List<UsuarioPublico> listarUsuarios() throws RemoteException {
        List<UsuarioPublico> lista = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 4) {
                    String login = partes[0];
                    String nome = partes[1];
                    String imagem = partes[2];
                    boolean online = Boolean.parseBoolean(partes[3]);

                    UsuarioPublico user = new UsuarioPublico(nome, imagem, online);
                    lista.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler usuários visíveis: " + e.getMessage());
        }

        return lista;
    }
}
