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
    private final String FILE_PEDIDOS = "server/data/grupos_pedidos.txt";

    // Mapa nomeGrupo -> Grupo
    private final Map<String, Grupo> grupos = new HashMap<>();
    // Mapa nomeGrupo -> usuários pendentes para aprovação
    private final Map<String, Set<String>> pedidosPendentes = new HashMap<>();

    public GrupoImpl() throws RemoteException {
        super();
        carregarGruposDoArquivo();
        carregarPedidosPendentesDoArquivo();
    }

    // --- CARREGAR/SALVAR GRUPOS ---
    private synchronized void carregarGruposDoArquivo() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 3);
                if (partes.length == 3) {
                    String nome = partes[0].trim();
                    String admin = partes[1].trim();
                    String membrosStr = partes[2].trim();

                    Grupo grupo = new Grupo(nome, admin);

                    grupo.membros.clear();

                    if (!membrosStr.isEmpty()) {
                        String[] membros = membrosStr.split(",");
                        for (String m : membros) {
                            String membroTrim = m.trim();
                            if (!membroTrim.isEmpty()) {
                                grupo.membros.add(membroTrim);
                            }
                        }
                    }
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

    // --- CARREGAR/SALVAR PEDIDOS PENDENTES ---
    private synchronized void carregarPedidosPendentesDoArquivo() {
        File file = new File(FILE_PEDIDOS);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 2);
                if (partes.length == 2) {
                    String nomeGrupo = partes[0].trim();
                    String usuariosStr = partes[1].trim();

                    Set<String> pendentes = new HashSet<>();
                    if (!usuariosStr.isEmpty()) {
                        String[] usuarios = usuariosStr.split(",");
                        for (String u : usuarios) {
                            String userTrim = u.trim();
                            if (!userTrim.isEmpty()) {
                                pendentes.add(userTrim);
                            }
                        }
                    }
                    pedidosPendentes.put(nomeGrupo, pendentes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void salvarPedidosPendentesNoArquivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PEDIDOS))) {
            for (Map.Entry<String, Set<String>> entry : pedidosPendentes.entrySet()) {
                String linha = entry.getKey() + ";" + String.join(",", entry.getValue());
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS PRINCIPAIS ---

    @Override
    public synchronized boolean criarGrupo(String nomeGrupo, String criador) throws RemoteException {
        if (nomeGrupo == null || nomeGrupo.trim().isEmpty()) return false;
        if (criador == null || criador.trim().isEmpty()) return false;
        if (grupos.containsKey(nomeGrupo)) return false;

        Grupo grupo = new Grupo(nomeGrupo.trim(), criador.trim());
        grupos.put(nomeGrupo.trim(), grupo);
        salvarGruposNoArquivo();
        return true;
    }

    @Override
    public synchronized List<String> listarGrupos() throws RemoteException {
        return new ArrayList<>(grupos.keySet());
    }

    @Override
    public synchronized boolean entrarNoGrupo(String nomeGrupo, String usuario) throws RemoteException {
        if (nomeGrupo == null || usuario == null) return false;
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null) return false;

        // Se já é membro, ok
        if (grupo.membros.contains(usuario)) return true;

        // Se já tem pedido pendente, não duplica
        Set<String> pendentes = pedidosPendentes.computeIfAbsent(nomeGrupo, k -> new HashSet<>());
        if (pendentes.contains(usuario)) return false; // já pediu entrada

        pendentes.add(usuario);
        salvarPedidosPendentesNoArquivo();
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
        if (nomeGrupo == null || usuario == null) return false;
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
        return grupo != null && usuario != null && usuario.equals(grupo.admin);
    }

    @Override
    public synchronized boolean banirUsuario(String nomeGrupo, String admin, String usuarioParaBanir) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null || admin == null || usuarioParaBanir == null) return false;
        if (!admin.equals(grupo.admin)) return false;
        boolean removido = grupo.membros.remove(usuarioParaBanir);
        if (removido) {
            salvarGruposNoArquivo();
            // Também remover dos pedidos pendentes caso esteja lá
            Set<String> pendentes = pedidosPendentes.get(nomeGrupo);
            if (pendentes != null) {
                pendentes.remove(usuarioParaBanir);
                salvarPedidosPendentesNoArquivo();
            }
        }
        return removido;
    }

    // --- NOVOS MÉTODOS PARA GERENCIAR PEDIDOS PENDENTES ---

    @Override
    public synchronized Set<String> listarPedidosPendentes(String nomeGrupo, String admin) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null || !admin.equals(grupo.admin)) return Collections.emptySet();
        return new HashSet<>(pedidosPendentes.getOrDefault(nomeGrupo, Collections.emptySet()));
    }

    @Override
    public synchronized boolean aprovarEntrada(String nomeGrupo, String admin, String usuario) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null || !admin.equals(grupo.admin)) return false;
        Set<String> pendentes = pedidosPendentes.get(nomeGrupo);
        if (pendentes == null || !pendentes.contains(usuario)) return false;

        pendentes.remove(usuario);
        grupo.membros.add(usuario);
        salvarGruposNoArquivo();
        salvarPedidosPendentesNoArquivo();
        return true;
    }

    @Override
    public synchronized boolean rejeitarEntrada(String nomeGrupo, String admin, String usuario) throws RemoteException {
        Grupo grupo = grupos.get(nomeGrupo);
        if (grupo == null || !admin.equals(grupo.admin)) return false;
        Set<String> pendentes = pedidosPendentes.get(nomeGrupo);
        if (pendentes == null || !pendentes.contains(usuario)) return false;

        pendentes.remove(usuario);
        salvarPedidosPendentesNoArquivo();
        return true;
    }
}
