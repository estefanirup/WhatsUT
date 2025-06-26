package server.model;

import java.io.Serializable;

public class UsuarioPublico implements Serializable {
    public String nome;
    public String imagem;
    public boolean online;

    public UsuarioPublico(String nome, String imagem, boolean online) {
        this.nome = nome;
        this.imagem = imagem;
        this.online = online;
    }
}