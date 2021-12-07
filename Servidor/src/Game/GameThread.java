package Game;

import Game.Controller.LogController;
import Game.Model.Pregunta;
import Game.Controller.QuizController;
import Game.Model.Jugador;
import Game.Model.Partida;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class GameThread extends Thread {

    Socket c;
    final static String ASIMCYPHERTYPE = "RSA";
    final static String SIMCYPHERTYPE = "DES";
    final static String SIGNATURETYPE = "SHA1WITHRSA";

    private static final Logger logger = Logger.getLogger("GameServer");

    public GameThread(Socket c) {
        this.c = c;
    }

    public void run() {
        try {
            //Iniciamos Logeo de eventos en fichero
            iniciar_log();

            //Creamos los flujos
            ObjectOutputStream oos = new ObjectOutputStream(c.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(c.getInputStream());


            //GENERAMOS PAR DE CLAVE ASIMETRICAS Y ENVIAMOS LA CLAVE PUBLICA
            KeyPairGenerator asimKeygen;
            asimKeygen = KeyPairGenerator.getInstance(ASIMCYPHERTYPE);
            KeyPair KP = asimKeygen.generateKeyPair();
            oos.writeObject(KP.getPublic());

            //RECIBIMOS LA CLAVE SIMETRICA DE COMUNICACION CREADA POR EL CLIENTE
            // LA DESENCRYPTAMOS Y LA DECODIFICAMOS
            //Ref -> https://stackoverflow.com/questions/5355466/converting-secret-key-into-a-string-and-vice-versa
            //Ref -> https://qastack.mx/programming/5355466/converting-secret-key-into-a-string-and-vice-versa
            byte[] cipheredSimKey = (byte[]) ois.readObject();
            Cipher cipher = Cipher.getInstance(ASIMCYPHERTYPE);
            cipher.init(Cipher.DECRYPT_MODE, KP.getPrivate());
            byte[] encodedSimKey = cipher.doFinal(cipheredSimKey);
            SecretKey simKey = new SecretKeySpec(
                    encodedSimKey,
                    0,
                    encodedSimKey.length,
                    SIMCYPHERTYPE);

            //*****************************************+
            //Apartir de aqui las comunicaciones se realizan exclusivamente
            // con clave Simetrica (excepto firma)
            //*****************************************


            //RECIBIMOS JUGADOR REGISTRADO EN CLIENTE
            cipher = Cipher.getInstance(SIMCYPHERTYPE);
            cipher.init(Cipher.DECRYPT_MODE, simKey);
            SealedObject jugadorCPHR = (SealedObject) ois.readObject();
            Object o = (Object)jugadorCPHR.getObject(cipher);
            Jugador jugador = (Jugador) o;

            System.out.println("Registro jugador " + jugador.getNick());

            //PREPARAMOS Y FIRMAMOS LAS INSTRUCCIONES (CLAVE PRIVADA ASIM)
            String instrucciones = "Mis instrucciones";
            Signature signature = Signature.getInstance(SIGNATURETYPE);
            signature.initSign(KP.getPrivate());
            signature.update(instrucciones.getBytes());
            byte[] instruccionesFirmadas = signature.sign();

            //ENVIAMOS INSTRUCCIONES DE JUEGO FIRMADAS AL CLIENTE
            oos.writeObject(instrucciones);
            oos.writeObject(instruccionesFirmadas);

            /**********************************************************
             * COMIENZA EL JUEGO
             * El servidor enviará preguntas al jugador hasta qeu este diga que no quiere más
             * El final se recogera con la lectura de la respuesta *FIN*
             * Las preguntas se envían sin cifrar y las respuestas se reciben cifradas.
             ********************************************************/

            //SELECCION NIVEL Y CATEGORIA
            //TODO SELECCION NIVEL Y CATEGORIA EN SERVIDOR
            int level = 4; //(1,2,3,4)
            int categoria = 0; //Categoria (Todas)

            //INICIALIZAR PARTIDA
            //ENVIAMOS ESTADO AL CLIENTE
            //ENVIAMOS MENSAJE DE CONFIRMACION PARA INICAR EL JUEGO
            Partida partida = new Partida(jugador, level, categoria);
            cipher = Cipher.getInstance(SIMCYPHERTYPE);
            cipher.init(Cipher.ENCRYPT_MODE, simKey);
            boolean gameIsAlive = partida.isAlive();
            oos.writeObject(new SealedObject(gameIsAlive, cipher));

            if (gameIsAlive) {

                //RECIBIMOS CONFIRMACION DEL CLIENTE PARA COMENZAR EL JUEGO
                cipher.init(Cipher.DECRYPT_MODE, simKey);
                SealedObject clientReadyCPHR = (SealedObject) ois.readObject();
                boolean seguirJugando = (boolean) clientReadyCPHR.getObject(cipher);

                if (seguirJugando) {
                    //EMPEZAR EL ENVIO DE PREGUNTAS Y REPCION DE RESPUESTAS
                    String clientResponse = "";

                    while (seguirJugando && gameIsAlive) {

                        //Cogemos una pregunta de la partida del jugador
                        // TODO BARAJAR RESPUESTAS (para evitar siempre el mismo orden)
                        Pregunta pregunta = partida.getNewPregunta();
                        //Collections.shuffle(pregunta.getOpciones());

                        //ENVIAMOS PREGUNTA AL CLIENTE
                        //ENVIAMOS EL NUMERO DE RESPUESTAS POSIBLES

                        oos.writeObject(
                                QuizController.getQuestionMessage(pregunta)
                        );
                        //SE envia un int como objeto porque si no falla..
                        oos.writeObject(pregunta.getOpciones().size());

                        //RECIBIMOS SI EL JUGADOR CONTINUA JUGANDO O NO
                        cipher.init(Cipher.DECRYPT_MODE, simKey);
                        SealedObject seguirJugandoCPHR = (SealedObject) ois.readObject();
                        seguirJugando = (boolean) seguirJugandoCPHR.getObject(cipher);

                        //SI EL JUGADOR HA RESPONDIDO !=*FIN*
                        if (seguirJugando) {
                            //RECIBIMOS REPUESTA A PREGUNTA REALIZADA
                            byte[] responseCPHR = (byte[]) ois.readObject();
                            String respuesta = new String(cipher.doFinal(responseCPHR));

                            //Tratamos la respuesta en la partida.
                            boolean isCorrecta;
                            isCorrecta = partida.responder(
                                    pregunta,
                                    pregunta.getOpciones()
                                            .get(Integer.parseInt(respuesta) - 1)
                                            .getDescripcion());

                            //ENVIAMOS AL CLIENTE SI HA ACERTADO O NO LA RESPUESTA
                            oos.writeBoolean(isCorrecta);

                            //ACTUALIZAMOS EL ESTADO DE LA PARTIDA (gameIsAlive)
                            //INFORMAMOS AL CLIENTE SOBRE EL ESTADO DE LA PARTIDA
                            gameIsAlive = partida.isAlive();
                            oos.writeBoolean(gameIsAlive);
                            if (!gameIsAlive) //Finalizada partida, no hay más pregutnas.
                                System.out.printf("Jugador %s ha respondido todas la preguntas.\n",jugador.getNick());

                        } else {
                            //FIN DE JUEGO CLIENTE ABANDONA PARTIDA COMENZADA
                            System.out.printf("Jugador %s abandona partida.\n",jugador.getNick());
                        }
                    }
                    //FIN DE JUEGO, ENVIAMOS ESTADISTICAS AL CLIENTE ( CIFRADAS)
                    System.out.printf("Enviando estadísticas a %S\n",jugador.getNick());
                    cipher.init(Cipher.ENCRYPT_MODE, simKey);
                    byte[] estatCPHR = cipher.doFinal(
                            partida.getEstatisdisticas().getBytes()
                    );
                    oos.writeObject(estatCPHR);
                } else {
                    System.out.println("Jugador ha rechaza inicio de partida");
                }
            } else {
                System.out.println("No se han podido cargar preguntas" +
                        "\nVerificar el repositorio y carga de prguntas.");
            }

            //CERRAMOS FLUJOS DE DATOS  Y SOCKET
            oos.close();
            ois.close();
            c.close();
            System.out.printf("Cerrada la sesion de juego con %S\n", jugador.getNick());

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }

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

