package Model;

import Game.Model.Pregunta;
import Game.QuizDataRepo;
import Game.QuizController;

import java.util.*;

public class Partida {

    private int puntuacion=0;
    private int level=0;
    private int category=0;
    private Jugador jugador;
    private Set<Pregunta> preguntasSet;
    private List<Pregunta> contestadasList;
    private int respOK=0;
    private int respFAIL=0;

    public Partida(Jugador jugador, int level, int category) {
        this.jugador = jugador;
        this.level = level;
        this.category = 0;
        preguntasSet = QuizController.getQsSetByLevelAndCategory(level,category);
        contestadasList = new ArrayList<>();
    }

    /**Retorna una pregunta aleatoria del Set de preguntas.
     *
     * @return Pregunta o null si no hay preguntas
     */
    public Pregunta getNewPregunta (){
            return preguntasSet
                    .stream().skip(new Random()
                            .nextInt(preguntasSet.size()))
                    .findFirst().orElse(null);
    }

    public boolean remainingQuestions(){
        return preguntasSet.size() != 0;
    }
    public boolean isAlive(){
        return preguntasSet.size() != 0;
    }

    /**
     * Funcion que evalua y procesa trata una respuesta dada dentro de la partida.
     * Extrae la pregunta del set de preguntas, la introduce en la
     * lista de respondidas
     * Devuelve si la respuesta proporcionada a la pregunta es correcta o no
     * Incrementa el marcador de puntos si la respuesta es correcta
     *
     * @param pr pregunta que se desea responder.
     * @param descripcionRespuesta respuesta dad a la pregunta
     * @return True / False si la repuesta es o no correcta.
     */
    public boolean responder(Pregunta pr, String descripcionRespuesta){
        boolean res=false;
        if (preguntasSet.contains(pr)){ preguntasSet.remove(pr); }
        if (evalRespuesta(pr, descripcionRespuesta)){
                puntuacion += pr.getIdNivel();
                respOK++;
                res = true;
        } else{
            respFAIL++;
        }
        contestadasList.add(pr);
        return res;
    }

    private boolean evalRespuesta(Pregunta pregunta, String descripcionRespuesta){
        String resOK = pregunta.getCorrectAnswer();
        return resOK.equalsIgnoreCase(descripcionRespuesta);
    }

    public String getEstatisdisticas(){
        int preguntasTotales = contestadasList.size()+ preguntasSet.size();
        int preguntasRealizadas= contestadasList.size();
        int preguntascorrectas = respOK;
        int preguntasfalladas = respFAIL;
        String nivel = QuizDataRepo.getNiveles().get(this.level);
        String categoria = "ALEATORIA- TOTUM REVOLUTUM";
        if (QuizDataRepo.getCategoriasMap().containsKey(this.category))
            categoria = QuizDataRepo.getCategoriasMap().get(this.category);


        StringBuilder estadisticas = new StringBuilder();
        estadisticas.append("\n**************************************************************************\n");
        estadisticas.append(
                String.format("Jugador: %S \tPartida Nivel: %S Categoria: %S\n",
                        jugador.getNick(),
                        nivel,
                        categoria)
        );
        estadisticas.append(
                String.format("Preguntas-> Respondidas: %d \tCorrectas: %d Fallidas: %d\n",
                        preguntasRealizadas,
                        preguntascorrectas,
                        preguntasfalladas)
        );
        estadisticas.append("\n**************************************************************************\n");
        return estadisticas.toString();
    }
}
