package server.model;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private int id;
    private int userId;
    private int destinatarioId;
    private String texto;
    private Date horario;
    private boolean lido;
    private String fileName; 
    private String fileMimeType; 
    private String fileContentBase64;
    
    public Message(int id, int userId, int destinatarioId, String texto) {
        this.id = id;
        this.userId = userId;
        this.destinatarioId = destinatarioId; //pode ser id de um grupo
        this.texto = texto;
        this.horario = new Date(); 
        this.lido = false;
        // Inicializar campos de arquivo como nulos por padrão
        this.fileName = null;
        this.fileMimeType = null;
        this.fileContentBase64 = null;
        
    }
    // Novo construtor para mensagens com arquivo
    public Message(int id, int userId, int destinatarioId, String texto, String fileName, String fileMimeType, String fileContentBase64) {
        this.id = id;
        this.userId = userId;
        this.destinatarioId = destinatarioId;
        this.texto = texto; // O texto pode ser uma legenda para o arquivo
        this.horario = new Date();
        this.lido = false;
        this.fileName = fileName;
        this.fileMimeType = fileMimeType;
        this.fileContentBase64 = fileContentBase64;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getDestinatarioId() { return destinatarioId; }
    public String getTexto() { return texto; }
    public Date getHorario() { return horario; }
    public boolean isLido() { return lido; }
    public void setLido(boolean lido) { this.lido = lido; }
    public void setHorario(Date date) { this.horario = date; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileMimeType() { return fileMimeType; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }

    public String getFileContentBase64() { return fileContentBase64; }
    public void setFileContentBase64(String fileContentBase64) { this.fileContentBase64 = fileContentBase64; }
}