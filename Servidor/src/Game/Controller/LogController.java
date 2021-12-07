package Game.Controller;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogController {

    private static Logger logger = Logger.getLogger("GameServer");

    public static Logger getLogger() { return logger;}

    public static void iniciar_log(){
        FileHandler fh;
        try {

            // Configuro el logger y establezco el formato
            fh = new FileHandler("Servidor/Logs/server.log", true);
            // fh=new FileHandler("./log_XML.log",true);
            logger.addHandler(fh);//para añadir las lineas del log al fichero
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

            // XMLFormatter formatter = new XMLFormatter ();
            SimpleFormatter formatter = new SimpleFormatter();

            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
