import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;
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
            //Apartir de aqui las comunicaciones se realizan con clave Simetrica
            //*****************************************


            //RECIBIMOS JUGADOR REGISTRADO EN CLIENTE
            cipher = Cipher.getInstance(SIMCYPHERTYPE);
            cipher.init(Cipher.DECRYPT_MODE, simKey);
            SealedObject jugadorCPHR = (SealedObject) ois.readObject();
            Jugador jugador = (Jugador) jugadorCPHR.getObject(cipher);

            System.out.println("Recibido jugador " + jugador.getNick());

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

            String clientResponse = "";
            boolean seguirJugando = true;
            boolean hayMasPreguntas = true;

            while (seguirJugando && hayMasPreguntas) {

                //ENVIAMOS PREGUNTA AL CLIENTE y SI QUEDAN MAS PREGUNTAS
                oos.writeObject("Aqui mandamos una pregunta del juego");
                //TODO modificacion variable hayMasPreguntas
                //oos.writeBoolean(hayMasPreguntas);
                oos.writeObject(hayMasPreguntas);


                //RECIBIMOS RESPUESTAS DEL CLIENTE Y LAS DESENCRIPTAMOS
                cipher.init(Cipher.DECRYPT_MODE, simKey);
                SealedObject seguirJugandoCPHR = (SealedObject) ois.readObject();
                seguirJugando = (boolean) seguirJugandoCPHR.getObject(cipher);

                if (seguirJugando) {
                    byte[] respuesta = (byte[]) ois.readObject();
                    String mensaje_descifrado = new String(cipher.doFinal(respuesta));

                    System.out.println(mensaje_descifrado);
                }
                else {
                    //ENVIAMOS RESULTADO PUNTUACION AL CLIENTE
                    //TODO
                    oos.writeObject("EStadisticas....");
                }
            }


            //RECIBIMOS RESPUESTA DEL CLIENTE Y LA DESENCIRPTAMOS
            byte[] mensaje = (byte[]) ois.readObject();

            cipher = Cipher.getInstance(SIMCYPHERTYPE);
            cipher.init(Cipher.DECRYPT_MODE, simKey);

            String mensaje_descifrado = new String(cipher.doFinal(mensaje));


            System.out.println("Mensaje descifrado con clave privada: " + mensaje_descifrado);

            oos.close();
            ois.close();
            c.close();
            System.out.println("Cerrada la sesion de juego");

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

