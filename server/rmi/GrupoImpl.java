package server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class GrupoImpl extends UnicastRemoteObject implements GrupoInterface {
    private static class Grupo {
        String nome;
        String admin;
        Set<String> membros;

        Grupo(String nome, String admin) {
            this.nome = nome;
            this.admin = admin;
            this.membros = new HashSet<>();
            this.membros.add(admin);
        }
    }

    private final String FILE_PATH = "server/data/grupos.txt";
    private final Map<String, Grupo> grupos = new HashMap<>();

    public GrupoImpl() throws RemoteException {
        super();
        carregarGruposDoArquivo();
    }

    private synchronized void carregarGruposDoArquivo() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 3);
                if (partes.length == 3) {
                    String nome = partes[0];
                    String admin = partes[1];
                    String[] membros = partes[2].split(",");
                    Grupo grupo = new Grupo(nome, admin);
                    grupo.membros.addAll(Arrays.asList(membros));
                    grupos.put(nome, grupo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void salvarGruposNoArquivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Grupo grupo : grupos.values()) {
                String linha = grupo.nome + ";" + grupo.admin + ";" + String.join(",", grupo.membros);
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean criarGrupo(String nomeGrupo, String criador) throws RemoteException {
        if (grupos.containsKey(nomeGrupo)) return false;
        Grupo grupo = new Grupo(nomeGrupo, criador);
        grupos.put(nomeGrupo, grupo);
        salvarGruposNoArquivo();
        return true;
    }

    @Override
    public synchronized List<String> listarGrupos() throws RemoteException {
        return new ArrayList<>(grupos.keySet());
    }

    @Override
    public synchronized boolean entrarNoGrupo(String nomeGrupo, String usuario) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null) return false;
        grupo.membros.add(usuario);
        salvarGruposNoArquivo();
        return true;
    }

    @Override
    public synchronized List<String> listarMembros(String nomeGrupo) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null) return Collections.emptyList();
        return new ArrayList<>(grupo.membros);
    }

    @Override
    public synchronized boolean sairDoGrupo(String nomeGrupo, String usuario) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null) return false;

        grupo.membros.remove(usuario);

        if (usuario.equals(grupo.admin)) {
            if (grupo.membros.isEmpty()) {
                grupos.remove(nomeGrupo);
            } else {
                grupo.admin = grupo.membros.iterator().next(); // novo admin
            }
        }

        salvarGruposNoArquivo();
        return true;
    }

    @Override
    public synchronized boolean ehAdmin(String nomeGrupo, String usuario) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        return grupo != null && usuario.equals(grupo.admin);
    }

    @Override
    public synchronized boolean banirUsuario(String nomeGrupo, String admin, String usuarioParaBanir) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null || !admin.equals(grupo.admin)) return false;
        boolean removido = grupo.membros.remove(usuarioParaBanir);
        if (removido) salvarGruposNoArquivo();
        return removido;
    }
}
