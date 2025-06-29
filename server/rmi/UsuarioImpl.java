package server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import server.model.UsuarioPublico;

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
                if (partes.length >= 5) {
                    String login = partes[0];
                    int id = Integer.parseInt(partes[1]);
                    String nome = partes[2];
                    String imagem = partes[3];
                    boolean online = Boolean.parseBoolean(partes[4]);

                    UsuarioPublico user = new UsuarioPublico(id, nome, imagem, online);
                    lista.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler usuários visíveis: " + e.getMessage());
        }

        return lista;
    }
}
