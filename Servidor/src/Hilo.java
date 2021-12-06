import Game.Model.Pregunta;
import Game.QuizController;
import Model.Jugador;
import Model.Partida;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Hilo extends Thread {

    Socket c;
    final static String ASIMCYPHERTYPE = "RSA";
    final static String SIMCYPHERTYPE = "DES";
    final static String SIGNATURETYPE = "SHA1WITHRSA";
    private Cipher cipher;
    private SecretKey simKey;

    public Hilo(Socket c) {
        this.c = c;
    }

    public void run() {
        try {
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
            cipher = Cipher.getInstance(ASIMCYPHERTYPE);
            cipher.init(Cipher.DECRYPT_MODE, KP.getPrivate());
            byte[] encodedSimKey = cipher.doFinal(cipheredSimKey);
            simKey = new SecretKeySpec(
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
            Jugador jugador = (Jugador) jugadorCPHR.getObject(cipher);

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
            //TODO
            int level = 1; //Nivel facil
            int categoria = 0; //Categoria (Todas)

            //INICIALIZAR PARTIDA
            //ENVIAMOS ESTADO AL CLIENTE
            //ENVIAMOS MENSAJE DE CONFIRMACION PARA INICAR EL JUEGO
            Partida partida = new Partida(jugador, 1, 0);
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

                        //ENVIAMOS PREGUNTA AL CLIENTE
                        //ENVIAMOS EL NUMERO DE RESPUESTAS POSIBLES
                        Pregunta pregunta = partida.getNewPregunta();
                        oos.writeObject(
                                QuizController.getQuestionMessage(pregunta)
                        );
                        oos.writeInt(pregunta.getOpciones().size());

                        // CONTROLAMOS E INFORMAMOS AL CLIENTE SOBRE EL ESTADO DE LA PARTIDA
                        gameIsAlive = partida.isAlive();
                        oos.writeObject(gameIsAlive);

                        //RECIBIMOS RESPUESTAS DEL CLIENTE Y LAS DESENCRIPTAMOS
                        cipher.init(Cipher.DECRYPT_MODE, simKey);
                        SealedObject seguirJugandoCPHR = (SealedObject) ois.readObject();
                        seguirJugando = (boolean) seguirJugandoCPHR.getObject(cipher);

                        if (seguirJugando) {
                            //CONTINUAR JUGANDO RECIBIMOS REPUESTA A PREGUNTA
                            byte[] responseCPHR = (byte[]) ois.readObject();
                            String respuesta = new String(cipher.doFinal(responseCPHR));

                            //Tratamos la respuesta en la partida.
                            boolean isCorrecta;
                            isCorrecta = partida.responder(
                                    pregunta,
                                    pregunta.getOpciones()
                                            .get(Integer.parseInt(respuesta)-1)
                                            .getDescripcion());

                            //ENVIAMOS AL CLIENTE SI HA ACERTADO O NO LA RESPUESTA
                            oos.writeBoolean(isCorrecta);

                        } else {
                            //FIN DE JUEGO
                            //ENVIAMOS RESULTADO PUNTUACION AL CLIENTE (CIFRADAS)
                            cipher.init(Cipher.ENCRYPT_MODE,simKey);
                            byte[] estatCPHR = cipher.doFinal(
                                    partida.getEstatisdisticas().getBytes()
                            );
                            oos.writeObject(estatCPHR);
                        }
                    }
                } else {
                    System.out.println("Jugador ha rechaza inicio de partida");
                }
            } else {
                System.out.println("No se han podido cargar preguntas" +
                        "\nVerificar el repositorio y carga de prguntas.");
            }

            oos.close();
            ois.close();
            c.close();
            System.out.printf("%s <--Cerrada la sesion de juego",jugador.getNick());

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }


}

