package server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import server.model.Grupo;

public class GrupoImpl extends UnicastRemoteObject implements GrupoInterface {

    private final String FILE_PATH = "server/data/grupos.txt";
    private final String FILE_PEDIDOS = "server/data/grupos_pedidos.txt";

    private final Map<Integer, Grupo> grupos = new HashMap<>();
    private final Map<Integer, Set<Integer>> pedidosPendentes = new HashMap<>();

    public GrupoImpl() throws RemoteException {
        super();
        carregarGruposDoArquivo();
        carregarPedidosPendentesDoArquivo();
    }

    // --- CARREGAR/SALVAR GRUPOS ---
    private synchronized void carregarGruposDoArquivo() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 6);
                if (partes.length == 6) {
                    int id = Integer.parseInt(partes[0].trim());
                    int idAdmin = Integer.parseInt(partes[1].trim());
                    String nome = partes[2].trim();
                    String descricao = partes[3].trim();
                    String criador = partes[4].trim();

                    List<Integer> membros = new ArrayList<>();
                    if (!partes[5].isEmpty()) {
                        membros = Arrays.stream(partes[5].split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Integer::parseInt)
                                .collect(Collectors.toList());
                    }

                    grupos.put(id, new Grupo(id, idAdmin, nome, descricao, criador, membros));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar grupo do arquivo: " + e.getMessage());
        }
    }

    private synchronized void salvarGruposNoArquivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Grupo grupo : grupos.values()) {
                String membrosStr = grupo.getMembros().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                String linha = String.join(";",
                        String.valueOf(grupo.getId()),
                        String.valueOf(grupo.getIdAdmin()),
                        grupo.getNome(),
                        grupo.getDescricao(),
                        grupo.getCriador(),
                        membrosStr);
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar grupos no arquivo: " + e.getMessage());
        }
    }

    // --- CARREGAR/SALVAR PEDIDOS PENDENTES ---
    private synchronized void carregarPedidosPendentesDoArquivo() {
        File file = new File(FILE_PEDIDOS);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", 2);
                if (partes.length == 2) {
                    int grupoId = Integer.parseInt(partes[0].trim());
                    Set<Integer> pendentes = Arrays.stream(partes[1].split(","))
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toSet());
                    pedidosPendentes.put(grupoId, pendentes);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar pedidos pendentes: " + e.getMessage());
        }
    }

    private synchronized void salvarPedidosPendentesNoArquivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PEDIDOS))) {
            for (Map.Entry<Integer, Set<Integer>> entry : pedidosPendentes.entrySet()) {
                String pendentesStr = entry.getValue().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                writer.write(entry.getKey() + ";" + pendentesStr);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar pedidos pendentes: " + e.getMessage());
        }
    }

    // --- MÉTODOS PRINCIPAIS ---
    @Override
    public synchronized boolean criarGrupo(int id, int idAdmin, String nome, String descricao, String criador,
            List<Integer> membros) throws RemoteException {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        int newId = grupos.isEmpty() ? 1 : Collections.max(grupos.keySet()) + 1;
        if (grupos.containsKey(newId)) {
            return false;
        }

        Grupo grupo = new Grupo(newId, idAdmin, nome.trim(), descricao, criador, membros);
        grupos.put(newId, grupo);
        salvarGruposNoArquivo();
        return true;
    }

    @Override
    public List<Grupo> listarGruposComDetalhes() {
        List<Grupo> gruposComDetalhes = new ArrayList<>();
        for (Integer grupoId : this.grupos.keySet()) {
            Grupo grupo = this.grupos.get(grupoId);
            gruposComDetalhes.add(new Grupo(
                    grupo.getId(),
                    grupo.getIdAdmin(),
                    grupo.getNome(),
                    grupo.getDescricao(),
                    grupo.getCriador(),
                    grupo.getMembros()));
        }
        return gruposComDetalhes;
    }

    @Override
    public synchronized List<Integer> listarGrupos() throws RemoteException {
        return new ArrayList<>(grupos.keySet());
    }

    @Override
    public synchronized boolean entrarNoGrupo(int grupoId, int usuarioId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        if (grupo == null) {
            return false;
        }

        if (grupo.isMembro(usuarioId)) {
            return true;
        }

        Set<Integer> pendentes = pedidosPendentes.computeIfAbsent(grupoId, k -> new HashSet<>());
        if (pendentes.contains(usuarioId)) {
            return false;
        }

        pendentes.add(usuarioId);
        salvarPedidosPendentesNoArquivo();
        return true;
    }

    @Override
    public synchronized List<Integer> listarMembros(int grupoId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        return grupo == null ? Collections.emptyList() : new ArrayList<>(grupo.getMembros());
    }

    @Override
    public synchronized List<Grupo> listarGruposDoUsuario(int usuarioId) throws RemoteException {
        List<Grupo> gruposDoUsuario = new ArrayList<>();

        for (Grupo grupo : grupos.values()) {
            if (grupo.getMembros().contains(usuarioId)) {
                gruposDoUsuario.add(new Grupo(
                        grupo.getId(),
                        grupo.getIdAdmin(),
                        grupo.getNome(),
                        grupo.getDescricao(),
                        grupo.getCriador(),
                        grupo.getMembros()));
            }
        }

        return gruposDoUsuario;
    }

    @Override
    public synchronized boolean sairDoGrupo(int grupoId, int usuarioId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        if (grupo == null) {
            return false;
        }

        if (!grupo.getMembros().remove(Integer.valueOf(usuarioId))) {
            return false;
        }

        if (usuarioId == grupo.getIdAdmin() && !grupo.getMembros().isEmpty()) {
            grupo.setIdAdmin(grupo.getMembros().iterator().next());
        } else if (grupo.getMembros().isEmpty()) {
            grupos.remove(grupoId);
        }

        salvarGruposNoArquivo();
        return true;
    }

    @Override
    public synchronized boolean ehAdmin(int grupoId, int usuarioId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        return grupo != null && usuarioId == grupo.getIdAdmin();
    }

    @Override
    public synchronized boolean banirUsuario(int grupoId, int adminId, int usuarioParaBanir) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        if (grupo == null || adminId != grupo.getIdAdmin()) {
            return false;
        }

        boolean removido = grupo.getMembros().remove(Integer.valueOf(usuarioParaBanir));
        if (removido) {
            salvarGruposNoArquivo();
            Set<Integer> pendentes = pedidosPendentes.get(grupoId);
            if (pendentes != null) {
                pendentes.remove(usuarioParaBanir);
                salvarPedidosPendentesNoArquivo();
            }
        }
        return removido;
    }

    @Override
    public synchronized Set<Integer> listarPedidosPendentes(int grupoId, int adminId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        if (grupo == null || adminId != grupo.getIdAdmin()) {
            return Collections.emptySet();
        }
        return new HashSet<>(pedidosPendentes.getOrDefault(grupoId, Collections.emptySet()));
    }

    @Override
    public synchronized boolean aprovarEntrada(int grupoId, int adminId, int usuarioId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        if (grupo == null || adminId != grupo.getIdAdmin()) {
            return false;
        }

        Set<Integer> pendentes = pedidosPendentes.get(grupoId);
        if (pendentes == null || !pendentes.remove(usuarioId)) {
            return false;
        }

        grupo.adicionarMembro(usuarioId);
        salvarGruposNoArquivo();
        salvarPedidosPendentesNoArquivo();
        return true;
    }

    @Override
    public synchronized boolean rejeitarEntrada(int grupoId, int adminId, int usuarioId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        if (grupo == null || adminId != grupo.getIdAdmin()) {
            return false;
        }

        Set<Integer> pendentes = pedidosPendentes.get(grupoId);
        if (pendentes == null || !pendentes.remove(usuarioId)) {
            return false;
        }

        salvarPedidosPendentesNoArquivo();
        return true;
    }

    @Override
    public synchronized String getNomeGrupo(int grupoId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        return grupo == null ? null : grupo.getNome();
    }

    @Override
    public synchronized int getIdAdmin(int grupoId) throws RemoteException {
        Grupo grupo = grupos.get(grupoId);
        return grupo == null ? -1 : grupo.getIdAdmin();
    }

    @Override
    public boolean ehParticipante(int grupoId, int usuarioId) throws RemoteException {

        Grupo grupo = grupos.get(grupoId);
        if (grupo == null)
            return false;

        List<Integer> membros = grupo.getMembros(); 
        return membros.contains(usuarioId);
    }

}
