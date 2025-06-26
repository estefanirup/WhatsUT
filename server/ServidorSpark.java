package server;

import server.rmi.UsuarioInterface;
import server.model.UsuarioPublico;

import static spark.Spark.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import com.google.gson.Gson;

public class ServidorSpark {
    public static void main(String[] args) {
        port(4567); // porta padrão Spark

        get("/api/usuarios", (req, res) -> {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                UsuarioInterface usuarioService = (UsuarioInterface) registry.lookup("UsuarioService");
                List<UsuarioPublico> usuarios = usuarioService.listarUsuarios();

                res.type("application/json");
                return new Gson().toJson(usuarios);
            } catch (Exception e) {
                res.status(500);
                return "{\"erro\":\"Erro ao listar usuários\"}";
            }
        });
    }
}
