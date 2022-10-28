package com.example.calcucomputo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Node {
    private static ServerSocket nodoSS;
    //private static int puerto = 1234;

    public static ArrayList<Socket> clientsList = new ArrayList<Socket>();
    public static ArrayList<ObjectOutputStream> activeOutputStreams = new ArrayList<ObjectOutputStream>();

    public static ArrayList<String> serverMsgs = new ArrayList<String>();
    public static ArrayList<String> serversID = new ArrayList<String>();
    public static ArrayList<String> clientsID = new ArrayList<String>();

    public static void main(String[] args)
    {
        ServerSocket nodoSS = null;
        // Lista de clients
        Random rand = new Random();
        int rNum=0;
        try {

            boolean puertoAbierto=false;
            while(!puertoAbierto){
                try {
                    rNum = rand.nextInt(7005-7000) + 7000;
                    nodoSS = new ServerSocket(rNum);
                    puertoAbierto = true;
                    System.out.println("Nodo creado con puerto: "+rNum);
                }
                catch(Exception e) {
                    
                }
            }

            nodoSS.setReuseAddress(true);

            // running infinite loop for getting
            // client request
            while (true) {

                if(clientsList == null){
                    System.out.println("Esperando conexiones en el puerto: " + Integer.toString(rNum));
                }
                else {
                    System.out.println("Conexiones con nodo("+rNum+"):");
                    System.out.println(clientsList);
                    System.out.println("----------------------------------");
                }

                // socket del cliente
                Socket cliente = nodoSS.accept();

                // nuevo cliente conectado
                System.out.println("Nueva conexion... ");
                // handler de cliente
                ClientHandler cs = new ClientHandler(cliente);

                // hilo distinto para cliente

                clientsList.add(cliente);
                System.out.println("Conexiones activas: " +clientsList.size());

                new Thread(cs).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (nodoSS != null) {
                try {
                    nodoSS.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket cs;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;

        //int clientsIteration = 0;//count to see the iterations between clients
        // Constructor
        public ClientHandler(Socket socket) throws IOException
        {
            this.cs = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            activeOutputStreams.add(out); // add out a la lista
        }

        public void run()
        {
            try {
                int serverCount = 0;
                //when you reach max count, it goes down to 0
                while(true){
                    //ouput a String
                    String msg = (String) in.readObject();

                    System.out.println("Conexiones activas: "+clientsList.size());


                    //agregar servidores cuando se conecte
                    if(msg.contains("Servidor")){
                        serversID.add(msg);
                    }
                    if(msg.contains("Cliente")){
                        clientsID.add(msg);
                    }
                    System.out.println("Servidores: "+serversID.size() +" // "+serversID);
                    System.out.println("Clientes: "+clientsID.size() +" // "+clientsID);


                    for (int i = 0; i < activeOutputStreams.size(); i++)
                    {
                        ObjectOutputStream temp_out = activeOutputStreams.get(i);
                        //revisar si hay 3 respuestas de servers

                        if(temp_out != out){
                            temp_out.writeObject(msg+":"+serversID.size());
                            System.out.println("Enviando mensaje: " + msg + " a las conexiones activas ( "+clientsList.size()+") "+  clientsList.get(i));
                            System.out.println("");
                        }
                    }
                    System.out.println("------");
                    System.out.println("------");

                }

            }
            catch (IOException e) {
                System.out.println("----------------------------------------------------------");
                System.out.println("*Conexion finalizada con: " + cs.getRemoteSocketAddress());
                String socketRemoved=String.valueOf(cs.getRemoteSocketAddress());
                String split[] = socketRemoved.split(":");
                for(int c=0 ;c< serversID.size();c=c+1){
                    String serverToRemove = serversID.get(c).replace("Servidor: ","");
                    String splitLoop[] = serverToRemove.split("localport=");
                    splitLoop[1] = splitLoop[1].replace("]","");
                    if(splitLoop[1].equals(split[1])){
                        System.out.println("Apagando servidor: "+serversID.remove(c));
                    }
                }
                for(int c=0 ;c< clientsID.size();c=c+1){
                    String serverToRemove = clientsID.get(c).replace("Servidor: ","");
                    String splitLoop[] = serverToRemove.split("localport=");
                    splitLoop[1] = splitLoop[1].replace("]","");

                    if(splitLoop[1].equals(split[1])){
                        System.out.println("Apagando cliente: "+clientsID.remove(c));
                    }
                }
                System.out.println("Servidores actualizados: "+serversID);
                System.out.println("Clientes actualizados: "+clientsID);
                clientsList.remove(cs);
                activeOutputStreams.remove(out);
            } catch (ClassNotFoundException ex) {

            }
        }
    }

}
