/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ficheroaccesomultiple;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * Pasamos argumentos por la linea de comandos: El primer argumento= el número
 * de orden de creación del proceso. El segundo argumento = ruta del fichero.
 * Voy a redirigir la salida estándar y la de error al fichero javalog.txt
 */
public class FicheroAccesoMultiple {

    public static void main(String[] args) {
        String nombreArchivo = ""; //nombre del archivo
        File archivo = null; //archivo
        int orden = 0;   //orden del proceso
        RandomAccessFile raf = null;  //para el acceso aleatorio al archivo
        FileLock bloqueo = null; //para bloquear el archivo
        int dato = 0; //valor inicial del dato

        //Compruebo si he recibido argumentos por la linea de comandos
        if (args.length > 0) {
            orden = Integer.parseInt(args[0]);
            //System.out.println (orden);
            //Número de orden de creación del proceso
        }
        try {
            //redirijo la salida estándar y de error usando System.setOut (PrinStream)
            //tengo en cuenta System.out es una referencia a PrintStream
            PrintStream ps = new PrintStream(
                    new BufferedOutputStream(
                            new FileOutputStream(
                                    new File("javalog.txt"), true)), true);
            System.setOut(ps);
            System.setErr(ps);
        } catch (Exception e) {
            System.err.println("Error. P " + orden + "No he podido redirigir la salida");
        }

        //1. referencia al file pasado como argumento
        if (args.length > 1) {
            nombreArchivo = args[1];
        } else {
            nombreArchivo = "prueba.txt";
        }

        archivo = new File(nombreArchivo);
        //preparo el acceso al fichero

        try {
            //2. Acceso aleatorio al archivo
            raf = new RandomAccessFile(archivo, "rwd");
            //abierto el archivo
            /**
             * ** SECCION CRITICA ***********
             */
                //Recupero el canal del archivo y lo bloqueo:
            //3. Bloqueo el canal
            bloqueo = raf.getChannel().lock();
            System.out.println("Proceso " + orden + " ENTRA en Sección Crítica. ");
                // leo el dato, le incremento, 
            //me coloco al principio del archivo y le escribo 
            raf.seek(0);
            if (raf.length()==0){
                dato=0;
            }
            else {
                dato=raf.readInt();
            }
        
            System.out.println("Dato leido = " + dato);
            dato++; //incremento en 1
            raf.seek(0); //me coloco al principio del archivo
            raf.writeInt(dato); //escribo el dato
            System.out.println("Proceso " + orden + " SALE de Sección Critica");
            //4. Libero bloqueo
            bloqueo.release();
            bloqueo = null;
            /**
             * *** FIN SECCIÓN CRITICA ******
             */
            System.out.println("Proceso " + orden + " Dato Escrito = " + dato);
            raf.close();
        } catch (Exception ex) {
            System.err.println("Error. Proceso " + orden + " No puede acceder al archivo");

        } finally { //me asegura del desbloqueo y el cierre del canal
            try {
                if (null != bloqueo) {
                    bloqueo.release();
                }
                if (null != raf) {
                    raf.close();
                }
            } catch (Exception e2) {
                System.err.println("Error cerrar fichero. Proceso" + orden);
                System.err.println(e2.toString());
                System.exit(-1);
            }

        }

    }
}
