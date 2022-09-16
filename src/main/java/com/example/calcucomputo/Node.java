package com.example.calcucomputo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Node {
    //server socket del cliente
    private static ServerSocket clientSS;
    //puerto en el que se comunicaran cliente y nodo
    private static int puerto = 1234;

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        
        //asignar server socket de cliente
        clientSS = new ServerSocket(puerto);
        
        //loop que escucha solicitudes indefinidamente hasta salga exit
        while (true) {
            System.out.println("Esperando conexion del cliente ");
            //aceptar conexion del socket del cliente y server socket
            Socket socket = clientSS.accept();
            
            //inicializar in y leer mensaje del cliente
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            //convertir mensaje del cliente a string
            String operacion = (String) in.readObject();

            String mensajeServidor = "";

            //hacer lista de los puertos de los servidores
            List<Integer> puertoServidor = new ArrayList<Integer>();
            puertoServidor.add(1235);
            puertoServidor.add(1236);

            //broadcast de la oepracion a los servidores
            for (int i = 0; i < 2; i++) {
                //obtener direccion de localhost
                InetAddress host = InetAddress.getLocalHost();
                Socket serverSocket = new Socket(host.getHostName(), puertoServidor.get(i));

                //inicializar el output para mandar la operacion al servidor
                ObjectOutputStream outServer = new ObjectOutputStream(serverSocket.getOutputStream());
                System.out.println("Mandando solicitud al servidor");
                outServer.writeObject(operacion); //data to send to the server

                //Recieve from server
                ObjectInputStream inServer = new ObjectInputStream(serverSocket.getInputStream());

                try {
                    mensajeServidor = (String) inServer.readObject();
                }
                catch(Exception e) {
                    System.out.println("Server regreso una excepcion "+e);
                }
                System.out.println("Respuesta: " + mensajeServidor);
            }

            System.out.println(" Mensaje recibido en el nodo: " + operacion);
            //inicializa el output del nodo
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            //manda la respuesta al cliente
            out.writeObject(""+mensajeServidor );
            //cierre de recursos
            in.close();
            out.close();
            socket.close();
            //cierra el nodo si recibe exit
            if (operacion.equalsIgnoreCase("exit")) break;
        }
        System.out.println("Cerrando Nodo");
        //cierre del SocketServer del Nodo
        clientSS.close();
    }
}
