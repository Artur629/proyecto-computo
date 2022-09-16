package com.example.calcucomputo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server1 {
    //ServerSocket del servidor
    private static ServerSocket serverSS;
    //Puerto en el que se comunicará con el nodo
    private static int puerto = 1235;
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        //declaracion del serverSocket en el puerto asignado
        serverSS = new ServerSocket(puerto);
        while (true) {
            System.out.println("Esperando la solicitud del nodo en Server1");
            //se crea el socket y se espera la conexión con el nodo
            Socket serverSocket = serverSS.accept();

            //se lee el input que mandó el nodo y se convierte a string
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());
            String operacion = (String) in.readObject();

            System.out.println("Operacion recibida en Server1: " + operacion);

            //Se inicialoza el output del servidor
            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            //Se llama al metodo para resolver la operaicon
            String resultado = CalcularResultado(operacion);
            //se regresa el resultado de la oepracion al nodo
            out.writeObject(resultado);
            //cierre de recursos
            in.close();
            out.close();
            serverSocket.close();
            //cierre del socketServer si se recibe como entrada "exit"
            if (operacion.equalsIgnoreCase("exit")) break;
        }
        System.out.println("Cerrando Server1");
        //cierre del SocketServer del servidor
        serverSS.close();
    }

    //metodo para caclular el resultado de la operacion
    public static String CalcularResultado(String operacion){
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
        float val, op = 0;

        String operator = "";
        for (int i = 0; i <= stringOperacion.size() - 1; i++) {

            try {

                val = Float.parseFloat(stringOperacion.get(i));
                switch (operator) {
                    case "+" -> op = op + val;
                    case "-" -> op = op - val;
                    case "/" -> op = op / val;
                    case "*" -> op = op * val;
                }

                if (i == 0) {
                    op = val;
                }
            } catch (Exception e) {
                operator = stringOperacion.get(i);
                Thread.currentThread().interrupt();
            }

        }
        resultado = Float.toString(op);
        return resultado;
    }
}
