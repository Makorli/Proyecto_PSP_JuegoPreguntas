import Game.Controller.QuizLoader;
import Game.GameThread;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

//-> https://github.com/misabnll/Trivia-mec/blob/master/todocont_triviamec.sql

public class ServerMain {

    public final static String ficheroDatosQuiz= "Servidor/src/Game/DataRepo/QuizData.xlsx";
    public final static int PORT=5000;

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        //SEGURIDAD SSL
        System.setProperty("javax.net.ssl.keyStore","Servidor/Certs/AlmacenSSL.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","123456");

        ///////////////////////////////////////////////
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(Level.OFF);
        ///////////////////////////////////////////////

        ServerSocket s;
        Socket c;
        s = new ServerSocket(PORT);
        try {
            QuizLoader.loadAll(ficheroDatosQuiz);
            System.out.println("Servidor iniciado correctamente.... listo para recibir locos por el conocimiento!");
            while (true) {
                c = s.accept(); //esperando cliente
                GameThread gameThread = new GameThread(c);
                gameThread.start();
            }
        } catch (IOException e ){
            System.out.println("Error en la carga de datos del servidor");
        }

    }

}
