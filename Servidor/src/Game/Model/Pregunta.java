package Game.Model;

import Game.Model.Respuesta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Pregunta {

    private int id;
    private int idCategoria;
    private int idNivel;
    private String Descripcion;
    private Respuesta respuesta;
    private List<Respuesta> opciones;

    public Pregunta() {
        opciones= new ArrayList<>();
    }

    public List<Respuesta> getOpciones() {
        return opciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public Respuesta getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(Respuesta respuesta) {
        this.respuesta = respuesta;
        this.opciones.add(respuesta);
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public int getIdNivel() {
        return idNivel;
    }

    public void setIdNivel(int idNivel) {
        this.idNivel = idNivel;
    }

    public String getCorrectAnswer(){
        String answer = "";
        for (Respuesta res: opciones){
            if (res.isCorrecta())
                return res.getDescripcion();
        }
        return answer;
    }
}
