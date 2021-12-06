package Game.Model;

public class Respuesta {

    private int id;
    private int idPregunta;
    private String Descripcion;
    private boolean correcta;

    public Respuesta(String descripcion) {
        Descripcion = descripcion;
    }

    public Respuesta() {
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getDescripcion() { return Descripcion; }

    public void setDescripcion(String descripcion) { Descripcion = descripcion; }

    public int getIdPregunta() { return idPregunta; }

    public void setIdPregunta(int idPregunta) { this.idPregunta = idPregunta; }

    public boolean isCorrecta() { return correcta; }

    public void setCorrecta(boolean correcta) { this.correcta = correcta; }
}
