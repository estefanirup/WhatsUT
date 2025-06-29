package server.model;

import java.io.Serializable;

public class UsuarioPublico implements Serializable {
    private int id;
    private String nome;
    private String imagem;
    private boolean online;

    public UsuarioPublico(int id, String nome, String imagem, boolean online) {
        this.id = id;
        this.nome = nome;
        this.imagem = imagem;
        this.online = online;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getImagem() {
        return imagem;
    }

    public boolean isOnline() {
        return online;
    }
}
