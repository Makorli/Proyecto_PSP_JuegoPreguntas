package Game;


import Game.Model.Pregunta;
import Game.Model.Respuesta;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
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

    public static void initDataRepo() throws IOException {QuizLoader.loadAll(); }

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

    private static class QuizLoader {

        private static final String myPathFile = "Servidor/src/Game/QuizData.xlsx";
        private static final File myFile = new File(myPathFile);

        public static void loadAll() throws IOException {
            loadLevels();
            loadCategories();
            loadQuestions();
            loadAnswers();
        }

        private static void loadLevels() throws IOException {

            //ABRIMOS WORKBOOK DEL EXCEL Y SELECCIONAMOS LA HOJA QUE VAMOS A RECORRER
            FileInputStream file = new FileInputStream(myFile);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet("Niveles");

            //RECORREMOS LAS HOJA DE CALCULO ITERANDO FILAS Y COLUMNAS Y RELIZANDO LA CARGA DE VALORES.
            //Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    System.out.println("Iniciando carga de Niveles...");
                } else {
                    int id = (int) row.getCell(0).getNumericCellValue();
                    String nivel = row.getCell(1).getStringCellValue();
                    System.out.print(id + ".- " + nivel+", ");
                    QuizDataRepo.addNivel(id, nivel);
                }
            }
            System.out.println();
        }

        private static void loadCategories() throws IOException {

            //ABRIMOS WORKBOOK DEL EXCEL Y SELECCIONAMOS LA HOJA QUE VAMOS A RECORRER
            FileInputStream file = new FileInputStream(myFile);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet("Categories");

            //RECORREMOS LAS HOJA DE CALCULO ITERANDO FILAS Y COLUMNAS Y RELIZANDO LA CARGA DE VALORES.
            //Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    System.out.println("Iniciando carga de categorías");
                } else {
                    int id = (int) row.getCell(0).getNumericCellValue();
                    String categoria = row.getCell(1).getStringCellValue();
                    System.out.print(id + ".- " + categoria+", ");
                    QuizDataRepo.addCategoria(id, categoria);
                }
            }
            System.out.println();


        }

        private static void loadQuestions() throws IOException {

            //ABRIMOS WORKBOOK DEL EXCEL Y SELECCIONAMOS LA HOJA QUE VAMOS A RECORRER
            FileInputStream file = new FileInputStream(myFile);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet("Questions");

            //RECORREMOS LAS HOJA DE CALCULO ITERANDO FILAS Y COLUMNAS Y RELIZANDO LA CARGA DE VALORES.
            //Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()){
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    System.out.print("Iniciando carga de preguntas..");
                } else {
                    int id = (int) row.getCell(0).getNumericCellValue();
                    String descripcion = row.getCell(1).getStringCellValue();
                    int idCategoria = (int) row.getCell(2).getNumericCellValue();
                    int idNivel = (int) row.getCell(3).getNumericCellValue();

                    Pregunta p = new Pregunta();
                    p.setId(id);
                    p.setDescripcion(descripcion);
                    p.setIdCategoria(idCategoria);
                    p.setIdNivel(idNivel);

                    QuizDataRepo.addPregunta(p.getId(), p);
                    System.out.print(".");
                }
            }
            System.out.println("\nCarga Preguntas finalizada");


        }

        private static void loadAnswers() throws IOException {
            //PReviamente se deben haber cargado las preguntas.

            //ABRIMOS WORKBOOK DEL EXCEL Y SELECCIONAMOS LA HOJA QUE VAMOS A RECORRER
            FileInputStream file = new FileInputStream(myFile);
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet("Answers");

            //RECORREMOS LAS HOJA DE CALCULO ITERANDO FILAS Y RELIZANDO LA CARGA DE VALORES.
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()){
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    System.out.print("Iniciando carga de respuestas..");
                } else {
                    int id = (int) row.getCell(0).getNumericCellValue();
                    int idPregunta = (int) row.getCell(1).getNumericCellValue();

                    Cell descripctionCell = row.getCell(2);
                    descripctionCell.setCellType(CellType.STRING);
                    String descripcion;
                    switch (descripctionCell.getCellType()){
                        case BOOLEAN-> descripcion = String.valueOf(row.getCell(2).getBooleanCellValue());
                        case NUMERIC-> descripcion = String.valueOf(row.getCell(2).getNumericCellValue());
                        default-> descripcion = String.valueOf(row.getCell(2).getStringCellValue());
                    }
                    int correcta = (int) row.getCell(3).getNumericCellValue();

                    Respuesta r = new Respuesta();
                    r.setId(id);
                    r.setIdPregunta(idPregunta);
                    r.setDescripcion(descripcion);
                    r.setCorrecta((correcta > 0));
                    //Añadimos la repsuesta a nuestro ddiccionario global de respuestas
                    QuizDataRepo.addRespueta(r.getId(), r);
                    System.out.print(".");
                    //Añadimos la respuesta como opcion en la pregunta especificada.
                    QuizDataRepo.getPreguntasMap().get(r.getIdPregunta()).getOpciones().add(r);
                }
            }
            System.out.println("\nCarga Respuestas finalizada");
        }

    }
}

