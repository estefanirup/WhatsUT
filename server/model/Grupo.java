package server.model;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {
    private String nome;
    private String descricao;
    private String criador;
    private List<String> membros;

    public Grupo(String nome, String descricao, String criador, List<String> membros) {
        this.nome = nome;
        this.descricao = descricao;
        this.criador = criador;
        this.membros = membros;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCriador() {
        return criador;
    }

    public List<String> getMembros() {
        return membros;
    }

    public void adicionarMembro(String login) {
        if (!membros.contains(login)) {
            membros.add(login);
        }
    }

    public void removerMembro(String login) {
        membros.remove(login);
    }

    public boolean isMembro(String login) {
        return membros.contains(login);
    }
}
