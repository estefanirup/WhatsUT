package server.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import utils.CryptoUtil;

public class AuthImpl extends UnicastRemoteObject implements AuthInterface {
    private final Map<String, String> usuarios = new HashMap<>();
    private final Map<String, Integer> usuarioIds = new HashMap<>();
    private final List<String> logados = new ArrayList<>();
    private final String FILE_PATH = "server/data/usuarios.txt";
    private final String VISIVEIS_PATH = "server/data/usuarios_visiveis.txt";
    private final String IMAGEM_PADRAO = "default.png";

    public AuthImpl() throws RemoteException {
        super();
        carregarUsuarios();

        // Cria usuário teste caso não exista
        if (!usuarios.containsKey("teste")) {
            try {
                registrar("teste", "123");
                System.out.println("Usuário 'teste' criado para teste.");
            } catch (RemoteException e) {
                System.out.println("Erro ao criar usuário de teste:");
                e.printStackTrace();
            }
        }
    }

    private void carregarUsuarios() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(":");
                if (partes.length == 2) {
                    usuarios.put(partes[0], partes[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Arquivo de usuários não encontrado, criando novo...");
        }

        File visiveisFile = new File(VISIVEIS_PATH);
        if (visiveisFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(visiveisFile))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    String[] partes = linha.split(";");
                    if (partes.length >= 2) {
                        String login = partes[0];
                        int id = Integer.parseInt(partes[1]);
                        usuarioIds.put(login, id);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Erro ao carregar IDs de usuários visíveis:");
                e.printStackTrace();
            }
        }
    }

    private void salvarUsuario(String nome, String senhaHash) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(nome + ":" + senhaHash);
            writer.newLine();
        }
    }

    private synchronized void atualizarUsuariosVisiveis() throws IOException {
        Map<String, String[]> visiveisMap = new HashMap<>();

        File file = new File(VISIVEIS_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    String[] partes = linha.split(";");
                    if (partes.length == 5) {
                        visiveisMap.put(partes[0], partes); // login -> {login, id, nome, imagem, online}
                    }
                }
            }
        }

        // Atualiza todos os usuários cadastrados no sistema
        for (String login : usuarios.keySet()) {
            String[] userData = visiveisMap.get(login);
            String id = (userData != null) ? userData[1] : String.valueOf(getNextId());
            String nomePublico = login; 
            String imagem = IMAGEM_PADRAO;
            String online = logados.contains(login) ? "true" : "false";

            visiveisMap.put(login, new String[] { login, id, nomePublico, imagem, online });
        }

        // Reescreve o arquivo com as informações atualizadas
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VISIVEIS_PATH, false))) {
            for (String[] partes : visiveisMap.values()) {
                writer.write(String.join(";", partes));
                writer.newLine();
            }
        }
    }

    @Override
    public synchronized boolean registrar(String nome, String senha) throws RemoteException {
        if (usuarios.containsKey(nome))
            return false;

        int nextId = getNextId();

        String senhaHash = CryptoUtil.hash(senha);
        usuarios.put(nome, senhaHash);
        usuarioIds.put(nome, nextId);
        try {
            salvarUsuario(nome, senhaHash);
            atualizarUsuariosVisiveis(); // Atualiza o arquivo incluindo o usuário com status offline
        } catch (IOException e) {
            throw new RemoteException("Erro ao salvar usuário");
        }
        return true;
    }

    @Override
    public synchronized boolean login(String nome, String senha) throws RemoteException {
        String senhaHash = CryptoUtil.hash(senha);
        if (senhaHash.equals(usuarios.get(nome))) {
            if (!logados.contains(nome)) {
                logados.add(nome);
                try {
                    atualizarUsuariosVisiveis(); // Atualiza o arquivo marcando usuário online
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public synchronized int getUserId(String username) throws RemoteException {
        return usuarioIds.getOrDefault(username, -1);
    }

    @Override
    public synchronized void logout(String nome) throws RemoteException {
        if (logados.contains(nome)) {
            logados.remove(nome);
            try {
                atualizarUsuariosVisiveis(); // Atualiza status offline no arquivo
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized List<String> listarUsuarios() throws RemoteException {
        return new ArrayList<>(logados);
    }

    private int getNextId() {
        int maxId = 0;
        File file = new File(VISIVEIS_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    String[] partes = linha.split(";");
                    if (partes.length >= 2) {
                        int id = Integer.parseInt(partes[1]);
                        if (id > maxId) maxId = id;
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return maxId + 1;
    }
}
