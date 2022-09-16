package com.example.calcucomputo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Cliente {
    
    //metodo que recibe la operación de la interfaz gráfica y lo manda
    //al nodo para que sea resuelta
    public String RecibirOperacion(String operacion) throws UnknownHostException,IOException {
        System.out.println("Operacion recibida:  "+operacion);
        
        //obtener direccion IP de localhost
        InetAddress host = InetAddress.getLocalHost();

        //iniciar socket
        Socket clientSocket = null;
        clientSocket = new Socket(host.getHostName(), 1234);

        //inicializar el output para mandar la operacion al nodo
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        System.out.println("Mandando operacion al nodo");
        //mandar operacion al nodo
        out.writeObject(operacion); 

        //inicializar listener y recibir respuesta del nodo
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        String opResuelta = "";
        try {
            //lee el mensaje recibido del nodo
            opResuelta = (String) in.readObject();
            System.out.println("Resultado: "+ opResuelta);
        }
        catch(Exception e) {
            System.out.println("Excepcion del nodo "+e);
        }

        //cierra los recursos de comunicacion
        in.close();
        out.close();
        try {
            Thread.sleep(100);
        }
        catch(Exception e) {
            System.out.println("Error de hilo "+e);
        }
        //regresa en un string el resultado de la operacion
        return  opResuelta;
    }
}
