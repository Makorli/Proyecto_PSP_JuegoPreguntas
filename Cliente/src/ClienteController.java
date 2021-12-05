import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClienteController {

    public static Jugador registerPlayer() {

        System.out.println("Registro de nuevo usuario \n");
        Jugador jugador = new Jugador();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String lectura;
        Pattern pat;
        Matcher mat;
        //Diccionario con las validaciones.
        List<Jugador.Validacion> campos = jugador.getValidaciones();
        boolean todok = false;
        for (Jugador.Validacion validacion : campos) {

            try {
                //LEEMOS LOS CAMPOR Y LOS MATCHEAMOS CON REGEX
                do {
                    System.out.printf("Introduce %s: ", validacion.getCampo());
                    lectura = br.readLine();
                    pat = validacion.getPattern();
                    mat = pat.matcher(lectura);
                    if (!mat.matches()) {
                        System.out.printf("Error en lectura de %s --> (* * %s * *)\n",
                                validacion.getCampo(),
                                validacion.getDescripcion());
                    } else {
                        //GUARDAMOS LOS VALORES DEL NUEVO JUGADOR
                        switch (validacion.getCampo()) {
                            case "nombre" -> jugador.setNombre(lectura);
                            case "apellido" -> jugador.setApellido(lectura);
                            case "edad" -> jugador.setEdad(Integer.parseInt(lectura));
                            case "nick" -> jugador.setNick(lectura);
                            case "password" -> jugador.setPassword(lectura);
                        }
                    }
                } while (!mat.matches());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                //logger.log(Level.SEVERE,"Error al leer el login del usuario" + e.getMessage());
            }
        }
        return jugador;
    }

}
