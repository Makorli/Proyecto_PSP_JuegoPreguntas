import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.Scanner;

public class Cliente {

    private final static int PORT = 5000;
    private final static String ASIMCYPHERTYPE = "RSA";
    private final static String SIMCYPHERTYPE = "DES";
    private final static String SIGNATURETYPE = "SHA1WITHRSA";
    private Cipher cipher;

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException {
        //Conectamos al cliente
        Socket socket = new Socket("localhost", PORT);
        // Creamos los flujos
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        //obtenemos la clave publica
        PublicKey publicKey = (PublicKey) ois.readObject();

        //GENERAMOS CLAVE SIMETRICA DE ENCRYPTACION
        // LA ENVIAMOS CIFRADA AL SERVIDOR CON LA CLAVE PRIVADA
        //Ref-> https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
        //Ref -> https://qastack.mx/programming/5355466/converting-secret-key-into-a-string-and-vice-versa
        SecretKey simkey = KeyGenerator.getInstance(SIMCYPHERTYPE).generateKey();
        Cipher cipher = Cipher.getInstance(ASIMCYPHERTYPE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipheredSimKey = cipher.doFinal(simkey.getEncoded());
        oos.writeObject(cipheredSimKey);

        //*****************************************+
        //Apartir de aqui las comunicaciones se realizan con clave Simetrica
        //*****************************************

        //Registramos usuario y Enviamos jugador al servidor
        Jugador jugador = ClienteController.registerPlayer();

        cipher = Cipher.getInstance(SIMCYPHERTYPE);
        cipher.init(Cipher.ENCRYPT_MODE, simkey);
        SealedObject jugadorCipher = new SealedObject(jugador, cipher);
        oos.writeObject(jugadorCipher);

        //RECIBIMOS INSTRUCCIONES DE JUEGO CO LA FIRMA
        String instrucciones = ois.readObject().toString();
        byte[] instruccionesFirmadas = (byte[]) ois.readObject();

        //COMPROBAMOS FIRMA INSTRUCCIONES CORRECTA (CLAVE PUBLICA ASIM)
        Signature signature = Signature.getInstance(SIGNATURETYPE);
        signature.initVerify(publicKey);
        signature.update(instrucciones.getBytes());
        boolean check = signature.verify(instruccionesFirmadas);

        //Compruebo la veracidad de la firma
        if (check) System.out.println("Las instrucciones son autenticas y podemos empezar a jugar...");
        else System.out.println("FIRMA NO VERIFICADA: Instrucciones recibidas manipuladas");

        /**********************************************************
         * COMENZAMOS A JUGAR
         * Recibiremos preguntas hasta que el jugador decida salir
         * escribiendo *FIN* en una respuesta o el servidor no tenga
         * más preguntas para el jugador.
         ********************************************************/

        String pregunta = "";
        String respuesta ="";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean hayMasPreguntas = true;

        boolean seguirjugando = true;
        cipher = Cipher.getInstance(SIMCYPHERTYPE);
        cipher.init(Cipher.ENCRYPT_MODE, simkey);
        SealedObject seguirJugandoCPHR = new SealedObject(seguirjugando, cipher);

        while (!respuesta.equalsIgnoreCase("*FIN*") && hayMasPreguntas) {
            //RECIBIMOS UNA PREGUNTA DEL SERVIDOR Y SI QUEDAN MAS
            pregunta = String.valueOf(ois.readObject());
            System.out.println("Aqui la pregunta!! " + pregunta);
            hayMasPreguntas = (boolean) ois.readObject();

            //LEEMOS RESPUESTA POR PANTALLA
            respuesta = br.readLine();

            if (!respuesta.equalsIgnoreCase("*FIN*")) {
                //CONTINUAMOS JUGANDO
                oos.writeObject(seguirJugandoCPHR);
                //RESPUESTA
                byte[] mensaje = cipher.doFinal(respuesta.getBytes());
                oos.writeObject(mensaje);

            } else {
                //FIN DE JUEGO
                seguirjugando = false;
                //byte[] sdf = new byte[]{(byte) (seguirjugando ? 1 : 0)};
                seguirJugandoCPHR = new SealedObject(seguirjugando, cipher);
                oos.writeObject(seguirJugandoCPHR);

                //RECIBIMOS ESTADISITICAS DATOS DE JUEGO
                //TODO
                System.out.println(String.valueOf(ois.readObject()));
            }


        }


        //Ciframos con la clave simetrica
        System.out.println("Escribe texto para cifrar con clave publica del servidor");
        Scanner sc = new Scanner(System.in);
        String texto = sc.nextLine();
        cipher = Cipher.getInstance(SIMCYPHERTYPE);
        cipher.init(Cipher.ENCRYPT_MODE, simkey);
        //directamente cifrarlo en un array de bytes, y no hacer conversiones a string
        byte[] mensaje = cipher.doFinal(texto.getBytes());
        oos.writeObject(mensaje);

        oos.close();
        ois.close();
        socket.close();

    }

}

