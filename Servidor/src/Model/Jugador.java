package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Jugador implements Serializable {

    private String nombre;
    private String apellido;
    private int edad;
    private String nick;
    private String password;

    private List<Validacion> validaciones;

    public Jugador() {
        validaciones= new ArrayList<>();
        validaciones.add(new Validacion(
                "nombre",
                "Entre 3 y 40 Caracteres Alfanumericos simples",
                Pattern.compile("[\\w\\s]{3,40}"))
        );
        validaciones.add(new Validacion(
                "apellido",
                "Entre 3 y 40 Caracteres Alfanumericos simples",
                Pattern.compile("[a-zA-Z\\s]{3,40}"))
        );
        validaciones.add(new Validacion(
                "edad",
                "Números enteros no negativos <100.",
                Pattern.compile("(\\d)[0-9]"))
        );
        validaciones.add(new Validacion(
                "nick",
                "Entre 3 y 8 Caracteres Alfanumericos simples",
                Pattern.compile("[a-zA-Z\\s]{3,8}"))
        );
        validaciones.add(new Validacion(
                "password",
                "La contraseña debe tener al entre 3 y 16 caracteres, al menos un dígito, al menos una minúscula y al menos una mayúscula.",
                Pattern.compile("^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{3,16}$"))
        );
    }

    public List<Validacion> getValidaciones() {
        return validaciones;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class Validacion implements Serializable{
        private String campo;
        private String Descripcion;
        private Pattern pattern;

        public Validacion(String campo, String descripcion, Pattern pattern) {
            this.campo = campo;
            Descripcion = descripcion;
            this.pattern = pattern;
        }

        public String getCampo() {
            return campo;
        }

        public void setCampo(String campo) {
            this.campo = campo;
        }

        public String getDescripcion() {
            return Descripcion;
        }

        public void setDescripcion(String descripcion) {
            Descripcion = descripcion;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }
    }
}
