package Game.Model;

import Game.Model.Respuesta;

import java.util.HashSet;

public class Pregunta {

    private String Descripcion;
    private Respuesta respuesta;
    private HashSet<Respuesta> opciones;

    public Pregunta() {
    }

    public Pregunta(String descripcion) {
        Descripcion = descripcion;
    }

    public HashSet<Respuesta> getOpciones() {
        return opciones;
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

    public void addOption(Respuesta respuesta){
        getOpciones().add(respuesta);
    }

    public void addOption(String respuesta){
        getOpciones().add(new Respuesta(respuesta));
    }
}
