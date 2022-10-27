package com.example.calcucomputo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server1 {
    //get the localhost IP address
    public static InetAddress host;
    public static Socket socket = null;
    public static ObjectOutputStream out = null;
    public static ObjectInputStream in = null;

    //socket server port on which it will listen
    private static int nodoPuerto = 1234;

    public static ArrayList<Socket> socketsList =
            new ArrayList<Socket>();
    public static ArrayList<ObjectOutputStream> outVector =
            new ArrayList<ObjectOutputStream>();
    public static ArrayList<ObjectInputStream> inVector =
            new ArrayList<ObjectInputStream>();

    public ArrayList<Thread> ossThreads =
            new ArrayList<Thread>();

    public static void main(String args[]) throws IOException, ClassNotFoundException{

        host = InetAddress.getLocalHost();
        for (int i = 7000; i <= 7005; i = i + 1) {
            //search for nodes between 5200 and 5000
            try {
                socket = new Socket(host.getHostName(), i);
                socketsList.add(socket);
                System.out.println("Conexion establecida con nodo en el puerto: " + Integer.toString(i));

                //read write from ObjectInputStream ObjectOutputStream objects
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                //out.writeObject("Servidor:"+ socket);
                inVector.add(in);
                outVector.add(out);
                out.writeObject("Servidor: "+socket);
            }
            catch(Exception e) {
                //nothing happens
            }
        }

        //keep listens indefinitely until receives 'exit' call or program terminates

        //send operation to all nodes

        while (true) {

            for (int j = 0; j < inVector.size(); j = j + 1) {
                System.out.println("Esperando solictud...");

                //convert ObjectInputStream object to String
                String msg = (String) inVector.get(j).readObject();

                System.out.println("Mensaje recibido en: "+msg);

                if(!(msg.contains("Servidor:")) && !(msg.contains("Cliente:")) ){

                    List<String> opMSG = QuitarEspacios(msg);


                    //if msg recieved contains RES, a result ignore it
                    float resValue = 0;
                    if (!msg.contains("RES")) {
                        opMSG.set(2, String.valueOf(opMSG.get(2).split(":")[0]));
                        if (msg.contains("+")) {
                            resValue = Float.parseFloat(opMSG.get(0)) + Float.parseFloat(opMSG.get(2));
                        } else if (msg.contains("-")) {
                            resValue = Float.parseFloat(opMSG.get(0)) - Float.parseFloat(opMSG.get(2));
                        } else if (msg.contains("*")) {
                            resValue = Float.parseFloat(opMSG.get(0)) * Float.parseFloat(opMSG.get(2));
                        } else if (msg.contains("/")) {
                            resValue = Float.parseFloat(opMSG.get(0)) / Float.parseFloat(opMSG.get(2));
                        }
                        System.out.println("Resultado a enviar: " + resValue);
                        outVector.get(j).writeObject("RES:" + Double.toString(resValue));

                        //terminate the server if client sends exit request
                        if (msg.equalsIgnoreCase("exit")) break;
                    }
                }
            }
        }

    }
    //metodo para caclular el resultado de la operacion
    public static List<String> QuitarEspacios(String operacion){
        String resultado = "";

        //caracter que se va a añadir a la lista de operacion
        Character aux;

        //string auxiliar para quitar los espacios de la operacion
        String numeroString = "";

        //array de la operacion sin espacios
        List<String> stringOperacion = new ArrayList<String>();

        for (int i = 0; i <= operacion.length() - 1; i++) {
            aux = operacion.charAt(i);
            //Si se encuentra un espacio en el string de operacion se elimina antes de añadirse a la lista
            if (aux == ' ') {
                stringOperacion.add(numeroString);
                numeroString = "";
            } else {
                numeroString = numeroString + aux;
            }
            if (operacion.length() - 1 == i) {
                stringOperacion.add(numeroString);
            }
        }
        
        return stringOperacion;
    }

}