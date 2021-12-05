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
        System.out.println("Servidor iniciado");
        while (true) {
            c = s.accept(); //esperando cliente
            Hilo hilo = new Hilo(c);
            hilo.start();
        }

    }

}
