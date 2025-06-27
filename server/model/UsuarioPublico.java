package server.model;

import java.io.Serializable;

public class UsuarioPublico implements Serializable {
    private String nome;
    private String imagem;
    private boolean online;

    public UsuarioPublico(String nome, String imagem, boolean online) {
        this.nome = nome;
        this.imagem = imagem;
        this.online = online;
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
