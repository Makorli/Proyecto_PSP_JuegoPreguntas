package Game.Model;

import Game.DataRepo.QuizDataRepo;
import Game.Controller.QuizController;

import java.util.*;

public class Partida {

    private int puntuacion=0;
    private int level=0;
    private int category=0;
    private final Jugador jugador;
    private final Set<Pregunta> preguntasSet;
    private final List<Pregunta> contestadasList;
    private int respOK=0;
    private int respFAIL=0;

    //Variable para mainupulación temporal de la
    //pregunta en curso de la partida
    private Pregunta tempQuestion;

    public Partida(Jugador jugador, int level, int category) {
        this.jugador = jugador;
        this.level = level;
        this.category = 0;
        preguntasSet = QuizController.getQsSetByLevelAndCategory(level,category);
        contestadasList = new ArrayList<>();
    }

    /**Retorna una pregunta aleatoria del Set de preguntas.
     * Selecciona una pregunta del set al azar
     * *NO* Baraja las respuestas de la pregunta para que no
     * esten siempre en el mismo orden.
     * @return Pregunta o null si no hay preguntas
     */
    public Pregunta getNewPregunta (){
        Pregunta p;

        int eleccionAzar=0;
        if (preguntasSet.size()>2)
            eleccionAzar= new Random().nextInt(preguntasSet.size());

        //Recuperamos una pregunta del set
        p= preguntasSet
                .stream().skip(eleccionAzar)
                .findFirst()
                .orElse(null);
        //TODO Barajamos las repuestas
        //if(p!=null)  Collections.shuffle(p.getOpciones());
        //Retornamos la pregunta
        return p;
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

        String comentarioGracioso;
        int porcentajeAciertos=(int) (respOK*100)/preguntasTotales;
        if(porcentajeAciertos<33)
            comentarioGracioso="A ver... entre tu y yo..deja de ver TeleCincoy ponte las Pilas!";
        else if (porcentajeAciertos<50)
            comentarioGracioso="Esta un poco justelas...justelas.. que no llegas!";
        else if (porcentajeAciertos<65)
            comentarioGracioso="Bien.. andas en la media de saber un poco de todo y mucho de nada.";
        else if (porcentajeAciertos<80)
            comentarioGracioso="Bueno bueno bueno, no te defiendees nada mal!. Muy bien!";
        else if (porcentajeAciertos<90)
            comentarioGracioso="Estas a tope!..lamentablemente no puedo darte un titulo universitario.";
        else if (porcentajeAciertos<100)
            comentarioGracioso="";
        else comentarioGracioso="Eres Dios!. Muy bien, lo sabes todo., ahora echate una pareja anda.";

            StringBuilder estadisticas = new StringBuilder();
        estadisticas.append("\n*******************************************************************************************\n");
        estadisticas.append(
                String.format("**  Partida -> Nivel: %S. \tCategoria: %S.\n",
                        jugador.getNick(),
                        nivel,
                        categoria)
        );
        estadisticas.append(
                String.format("**  Jugador -> Nick: %S \tPuntuación: %S puntos.\n",
                        jugador.getNick(),
                        nivel,
                        puntuacion)
        );
        estadisticas.append(
                String.format("**  Preguntas -> Respondidas: %d de %d. \tCorrectas: %d(%d%%). \tFallidas: %d(%d%%).\n",
                        preguntasTotales,
                        preguntasRealizadas,
                        preguntascorrectas,
                        porcentajeAciertos,
                        preguntasfalladas,
                        100-porcentajeAciertos)
        );
        estadisticas.append( String.format("**  Valoracion --> %S \n", comentarioGracioso));
        estadisticas.append("*******************************************************************************************\n");
        return estadisticas.toString();
    }
}
