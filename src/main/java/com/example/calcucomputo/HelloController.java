package com.example.calcucomputo;

import javafx.application.Platform;
import javafx.css.StyleableStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class HelloController {
    @FXML
    private TextField txt_result;

    public InetAddress host;
    public Socket socket = null;
    public ObjectOutputStream oos = null;
    public ObjectInputStream ois = null;

    public ArrayList<Socket> socketsList =
            new ArrayList<Socket>();
    public ArrayList<ObjectOutputStream> oosVector =
            new ArrayList<ObjectOutputStream>();
    public ArrayList<ObjectInputStream> oisVector =
            new ArrayList<ObjectInputStream>();

    public ArrayList<Thread> ossThreads =
            new ArrayList<Thread>();

    public void initialize() throws IOException, ClassNotFoundException {
        txt_result.setText("");

        //get the localhost IP address
        host = InetAddress.getLocalHost();

        // Create socket
        //nodes, we have nodes from 5200 to 5000 ports
        for (int i = 7000; i <= 7005; i = i + 1) {
            //search for nodes between 5200 and 5000
            try {

                socket = new Socket(host.getHostName(), i);
                socketsList.add(socket);
                System.out.println("Conexion establecida con nodo: " + Integer.toString(i));
                //Objects OI Stream
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                oosVector.add(oos);
                oisVector.add(ois);
                oos.writeObject("Cliente: "+socket);
                //Listening thread ObjectInputStream vector
                t.start();
                ossThreads.add(t);
                System.out.println("si");
            }
            catch(Exception e) {
                //nothing happens
            }
        }

    }

    //metodo para hacer reset a la calculadora y ponerla en 0
    private boolean CisPressed (Button numero){
        if (numero.getText().equals("C")){
            return true;
        } else {
            return false;
        }
    }

    //metodo para que cuando se escriba un numero se quite el cero inicial

    private void QuitarCero(){
        String display = txt_result.getText();
        if (display.startsWith("0")){
            txt_result.setText(display.substring(1));
        }
    }
    //metodo que obtiene el actionable event de la calculadora y escribe los numeros
    @FXML
    public void Number (ActionEvent aEvent){
        Object boton = aEvent.getSource();
        Button numero = (Button) boton;
        if (!CisPressed(numero)){
            QuitarCero();
            txt_result.setText(txt_result.getText()+numero.getText());
        } else{
            txt_result.setText("0");
        }
    }
    //metodo que obtiene el actionable event de la calculadora y escribe el operador
    //o manda la operación al cliente para que empiece el proceso de resolverlo
    @FXML
    public void Operator (ActionEvent aEvent) throws IOException{
        Object button = aEvent.getSource();
        Button operador = (Button) button;
        if (operador.getText().equals("=")){
            MandarAlCliente(aEvent);
        } else {
            txt_result.setText(txt_result.getText() + " " + operador.getText() + " ");
        }
    }

    //metodo para llamar al cliente
    public void MandarAlCliente(ActionEvent aEvent) throws IOException {
        MandarAlNodo(txt_result.getText(), aEvent);
    }

    //metodo para mandar la operación al cliente y regresar el resultado de la operación
    private void MandarAlNodo(String num, ActionEvent aEvent) throws IOException {
        String res = "";
        String res2 = "";
        num = txt_result.getText();
        aEvent.consume();

        //operacion should not contain other symbols than */+- and numbers
        if(!num.matches(".*[a-zA-Z].*")){

            // write to socket using ObjectOutputStream
            for (int i =0; i <oosVector.size(); i = i + 1) {
                //send operation to all nodes
                System.out.println("Enviando operacion al nodo: "+socketsList.get(i));
                oosVector.get(i).writeObject(num);
            }

        }
        else {
            txt_result.setText("Error en la expresión introducida");
        }

    }

    Thread t = new Thread(() -> {
        //Here write all actions that you want execute on background
        while(true){

            String message;
            try {
                message = (String) ois.readObject();
                String[] resSplit = message.split(":"); // {type of message},{content}

                if(resSplit[0].equals("RES")){
                    System.out.println("Resultado: "+resSplit[1]);
                    System.out.println("Numero de servidores: "+Integer.parseInt(resSplit[2]));
                }
                if(resSplit[0].equals("RES") && (Integer.parseInt(resSplit[2]))>2){
                    Platform.runLater(() -> txt_result.setText(resSplit[1]));
                }

            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }

        }

    });


}