import Game.QuizController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

//-> https://github.com/misabnll/Trivia-mec/blob/master/todocont_triviamec.sql

public class ServerMain {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        ServerSocket s;
        Socket c;
        s = new ServerSocket(5000);
        try {
            QuizController.initDataRepo();
            System.out.println("Servidor iniciado correctamente.... listo para recibir locos por el conocimiento!");
            while (true) {
                c = s.accept(); //esperando cliente
                Hilo hilo = new Hilo(c);
                hilo.start();
            }
        } catch (IOException e ){
            System.out.println("Error en la carga de datos del servidor");
        }

    }

}
