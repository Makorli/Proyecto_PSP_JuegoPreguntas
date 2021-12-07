package Game.DataRepo;

import Game.Model.Pregunta;
import Game.Model.Respuesta;
import java.util.HashMap;
import java.util.Map;

public class QuizDataRepo {

    private static final Map<Integer, Pregunta> preguntasMap = new HashMap();
    private static final Map<Integer, Respuesta> respuestasMap = new HashMap();
    private static final Map<Integer,String> nivelesMap = new HashMap<>();
    private static final Map<Integer,String> categoriasMap = new HashMap<>();


    public static Map<Integer,Pregunta> getPreguntasMap() { return preguntasMap; }
    public static Map<Integer,Respuesta> getRespuestasMap() {
        return respuestasMap;
    }
    public static Map<Integer,String> getNiveles() { return nivelesMap; }
    public static Map<Integer,String> getCategoriasMap() { return categoriasMap; }

    public static void addPregunta(int id, Pregunta pregunta){ preguntasMap.put(id, pregunta); }
    public static void addRespueta(int id, Respuesta respuesta){ respuestasMap.put(id, respuesta);}
    public static void addNivel(int id, String nivel){ nivelesMap.putIfAbsent(id, nivel); }
    public static void addCategoria(int id, String categoria){ categoriasMap.put(id, categoria); }
}
