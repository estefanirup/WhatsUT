package server.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String nome;
    private String imagem;
    private boolean online;
    private String ultimaMsg;
    private String hora;

    public Usuario(String nome, String imagem, boolean online, String ultimaMsg, String hora) {
        this.nome = nome;
        this.imagem = imagem;
        this.online = online;
        this.ultimaMsg = ultimaMsg;
        this.hora = hora;
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

    public String getUltimaMsg() {
        return ultimaMsg;
    }

    public String getHora() {
        return hora;
    }
}
