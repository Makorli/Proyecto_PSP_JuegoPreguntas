package Game;

import Game.Model.Pregunta;
import Game.Model.Respuesta;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.*;
import java.util.Iterator;

public class CargarQuiz {

    public static void main(String[] args) {

        Pregunta p;

        p= new Pregunta();
        p.setDescripcion("2x2?");
        p.setRespuesta(new Respuesta("4"));
        p.addOption("3");
        p.addOption("5");

        p= new Pregunta();
        p.setDescripcion("4x4?");
        p.setRespuesta(new Respuesta("16"));
        p.addOption("72");
        p.addOption("62");






    }

    public static class QuizLoader {

        public static void main(String[] args) {

            try {

                FileInputStream file = new FileInputStream(new File("C:\\test.xls"));

                //Get the workbook instance for XLS file
                HSSFWorkbook workbook = new HSSFWorkbook(file);

                //Get first sheet from the workbook
                HSSFSheet sheet = workbook.getSheetAt(0);

                //Iterate through each rows from first sheet
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    //For each row, iterate through each columns
                    Iterator <Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {

                        Cell cell = cellIterator.next();

                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_BOOLEAN:
                                System.out.print(cell.getBooleanCellValue() + "\t\t");
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                System.out.print(cell.getNumericCellValue() + "\t\t");
                                break;
                            case Cell.CELL_TYPE_STRING:
                                System.out.print(cell.getStringCellValue() + "\t\t");
                                break;
                        }
                    }
                    System.out.println("");
                }
                file.close();
                FileOutputStream out =
                        new FileOutputStream(new File("C:\\test.xls"));
                workbook.write(out);
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
