package Game;

import Game.Model.Pregunta;

import java.util.HashSet;
import java.util.Set;

public class Quiz {

    public static final Set<Pregunta> preguntas= new HashSet<>();

    public static HashSet<Pregunta> getPreguntas() {
        return (HashSet<Pregunta>) preguntas;
    }

    public static void addPregunta(Pregunta pregunta){
        getPreguntas().add(pregunta);
    }
}
