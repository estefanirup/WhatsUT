package server.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import server.model.UsuarioPublico;

import java.io.*;
import java.nio.file.*;

public class UsuarioImpl extends UnicastRemoteObject implements UsuarioInterface {

    public UsuarioImpl() throws RemoteException {
        super();
    }

    @Override
    public List<UsuarioPublico> listarUsuarios() throws RemoteException {
        List<UsuarioPublico> usuarios = new ArrayList<>();
        Path path = Paths.get("server/data/usuarios_visiveis.txt");

        if (!Files.exists(path)) {
            System.out.println("⚠️ Arquivo usuarios_visiveis.txt não encontrado.");
            return usuarios;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length == 3) {
                    String nome = partes[0].trim();
                    String imagem = partes[1].trim();
                    boolean online = Boolean.parseBoolean(partes[2].trim());
                    usuarios.add(new UsuarioPublico(nome, imagem, online));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler usuarios_visiveis.txt: " + e.getMessage());
        }

        return usuarios;
    }
}