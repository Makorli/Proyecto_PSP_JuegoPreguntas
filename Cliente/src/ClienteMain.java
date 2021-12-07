import Game.Controller.ClienteController;
import Game.Model.Jugador;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClienteMain {

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

        //SELECCION DE NIVEL y CATEGORIA
        //TODO SELECCION NIVEL Y CATEGORIA EN CLIENTE


        //RECIBIMOS DEL SERVIDOR
        // CONFIRMACION DE SI PARTIDA ESTA INICIADA O NO
        cipher.init(Cipher.DECRYPT_MODE, simkey);
        SealedObject partidaOKCPHR = (SealedObject) ois.readObject();
        boolean gameIsAlive = (boolean) partidaOKCPHR.getObject(cipher);

        if (gameIsAlive) {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String respuesta = "";

            //MENSAJE EN PANTALLA PARA EMPEZAR A JUGAR
            //ENVIO DE MENSAJE PARA EMPEZAR O ANULAR PARTIDA
            Pattern pat = Pattern.compile("[S]|[s]|[N]|[n]");
            Matcher mat;
            System.out.print("Estamos preparados!...");
            do {
                System.out.print("Comenzar Juego? (S/N): ");
                respuesta = br.readLine();
                mat = pat.matcher(respuesta);
            } while (!mat.matches());
            boolean seguirJugando = !respuesta.equalsIgnoreCase("N");
            //Enviamos la respuesta "booleana al servidor"
            cipher = Cipher.getInstance(SIMCYPHERTYPE);
            cipher.init(Cipher.ENCRYPT_MODE, simkey);
            SealedObject seguirJugandoCPHR = new SealedObject(seguirJugando, cipher);
            oos.writeObject(seguirJugandoCPHR);

            if (seguirJugando) {
                //EMPEZAR LA RECEPCION DE PREGUNTAS Y ENVIO DE RESPUESTAS
                String pregunta = "";
                int nOpciones=0;

                while (seguirJugando && gameIsAlive) {
                    //RECIBIMOS UNA PREGUNTA DEL SERVIDOR
                    //RECIBIMOS EL NUMERO DE RESPUESTAS POSIBLES
                    pregunta = String.valueOf(ois.readObject());
                    System.out.println("REcibiendo opciones");
                    nOpciones= (int) ois.readObject();

                    //MOSTRAMOS LA PREGUNTA EN PANTALLA LEEMOS Y VALIDAMOS
                    //RESPUESTA POR TECLADO
                    System.out.println(pregunta);
                    respuesta = ClienteController.readAnswer(nOpciones);

                    //INTERPRETAMOS RESPUESTA DE JUGADOR INTRODUCIDA POR TECLADO
                    // SI EL JUGADOR QUIERE SEGUIR JUGANDO O NO
                    seguirJugando = (!respuesta.equalsIgnoreCase("*FIN*"));

                    //ENVIAMOS AL SERVIDOR SI SE DESEA CONTINUAR O NO CON LA PARTIDA
                    seguirJugandoCPHR = new SealedObject(seguirJugando, cipher);
                    oos.writeObject(seguirJugandoCPHR);

                    if (seguirJugando) {
                        //CONTINUAMOS JUGANDO ENVIAMOS RESPUESTA A PREGUNTA
                        byte[] responseCPHR = cipher.doFinal(respuesta.getBytes());
                        oos.writeObject(responseCPHR);

                        //RECIBIMOS SI LA RESPUESTA DADA ES CORRECTA O NO DESDE EL SERVIDOR
                        boolean isCorrecta= ois.readBoolean();
                        if (isCorrecta)
                            System.out.println("Respuesta CORRECTA!");
                        else
                            System.out.println("Error!, tranquilo no pasa nada.");

                        //RECIBIMOS DEL SERVIDOR ESTADO DE LA PARTIDA (gameIsAlive)
                        gameIsAlive = ois.readBoolean();

                        if (!gameIsAlive) //Te has pasado el juego!
                            System.out.println("No hay más preguntas, has acabado con todas!");

                    } else {
                        //FIN DE JUEGO, JUGADOR ABANDONA LA PARTIDA
                        System.out.println("Cancelada partida en curso, otra vez acabarás...");
                    }
                }
                //RECIBIMOS ESTADISITICAS DATOS DE JUEGO CIFRADAS
                byte[] estatCPHR = (byte[]) ois.readObject();
                cipher.init(Cipher.DECRYPT_MODE,simkey);
                String estadisticas = new String(cipher.doFinal(estatCPHR));
                System.out.println(estadisticas);
                System.out.println("Vuelve Pronto!..si te atreves");
            } else {
                System.out.println("..Vaya! te tienes que ir. Aio!");
            }
        }

        oos.close();
        ois.close();
        socket.close();

    }

}

