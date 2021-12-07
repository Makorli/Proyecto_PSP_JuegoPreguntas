package Game.Controller;

import Game.DataRepo.QuizDataRepo;
import Game.Model.Pregunta;
import Game.Model.Respuesta;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class QuizLoader {

    private static File myFile;

    public static void loadAll(String myCustomPathFile) throws IOException {
        myFile = new File(myCustomPathFile);
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
