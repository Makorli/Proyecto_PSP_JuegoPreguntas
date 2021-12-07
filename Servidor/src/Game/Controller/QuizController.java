package Game.Controller;

import Game.DataRepo.QuizDataRepo;
import Game.Model.Pregunta;
import Game.Model.Respuesta;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase de carga de preguntas dentro del juego.
 * Lectura de un fichero (WorkBook) con diferentes hojas (Sheets) conteniendo
 * las preguntas y las respuestas y demás información relevante.
 * HOjas -> Questions, Answers, Categories, Niveles.
 *
 * https://www.viralpatel.net/java-read-write-excel-file-apache-poi/#google_vignette
 */
public class QuizController {

    public static Set<Pregunta> getAllQuestionsSet(){
        return new HashSet<>(QuizDataRepo.getPreguntasMap().values());
    }

    public static Set<Pregunta> getAllQsSetByLevel(int level){
        HashSet<Pregunta> qbylevelSet = new HashSet<>();
        for (Pregunta p: QuizDataRepo.getPreguntasMap().values()){
            if (p.getIdNivel()<= level) qbylevelSet.add(p);
        }
        return qbylevelSet;
    }

    public static Set<Pregunta> getQsSetByCategory(int category){
        if (category<=0) return getAllQuestionsSet();
        HashSet<Pregunta> qbylevelSet = new HashSet<>();
        for (Pregunta p: QuizDataRepo.getPreguntasMap().values()){
            if (p.getIdCategoria()== category) qbylevelSet.add(p);
        }
        return qbylevelSet;
    }

    public static Set<Pregunta> getQsSetByLevelAndCategory(int level, int category){
        if (category<=0) return getAllQsSetByLevel(level);
        HashSet<Pregunta> qbylevelSet = new HashSet<>();
        for (Pregunta p: QuizDataRepo.getPreguntasMap().values()){
            if (p.getIdCategoria()== category && p.getIdNivel()<=level)
                qbylevelSet.add(p);
        }
        return qbylevelSet;
    }

    /**
     * Procedimiento que retorna un String con la pregunta
     * su tipo , su nivel y las posibles repuestas.
     * Las respuestas iran precedidas de un PREFIJO numerico
     * para poder seleccionar posteriormente una opcion.
     * @param q objeto pregunta a trascribir.
     * @return String con la pregunta
     */
    public static String getQuestionMessage(Pregunta q){
        StringBuilder questionStrB= new StringBuilder();
        //Anadimos Nivel Categoria
        questionStrB.append(
                String.format("\nNivel: %s\t Categoría: %s\n",
                        QuizDataRepo.getNiveles().get(q.getIdNivel()),
                        QuizDataRepo.getCategoriasMap().get(q.getIdCategoria())
                ));
        //Anadimos Pregunta
        questionStrB.append(
                String.format("%s\n",q.getDescripcion()));
        //Añadimos las opciones de respuesta con un prefijo numerico
        //y con tabulación
        int prefijo = 1;
        for (Respuesta r: q.getOpciones()){
            questionStrB.append(
                    String.format("%d.- %s\n",prefijo,r.getDescripcion()
                    ));
            prefijo++;
        }
        return questionStrB.toString();
    }


}

