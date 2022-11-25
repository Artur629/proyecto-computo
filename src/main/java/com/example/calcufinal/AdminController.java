package com.example.calcufinal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class AdminController implements Initializable {

    private static ServerSocket adminSocket;
    static int port = 4000;
    private static int nodePort = 5000;
    private static final Set<String> servers = new HashSet<>();

    public Button closeButton;
    @FXML
    private TextField minSumaAcuse;
    @FXML
    private TextField minRestaAcuse;
    @FXML
    private TextField minMultAcuse;
    @FXML
    private TextField minDivAcuse;
    @FXML
    private ChoiceBox<String> serverChoice;
    @FXML
    private ChoiceBox<String> microserviceChoice;
    

    @FXML
    void setAcuses() {
        int acusesSuma = Integer.parseInt(minSumaAcuse.getText());
        int acusesResta = Integer.parseInt(minRestaAcuse.getText());
        int acusesMult = Integer.parseInt(minMultAcuse.getText());
        int acusesDiv = Integer.parseInt(minDivAcuse.getText());
        Package packet = new Package('A', port);
        packet.setAcuses(acusesSuma, acusesResta, acusesMult, acusesDiv);
        sendPackage(packet);
    }

    @FXML
    void setService() throws IOException {
        String server = serverChoice.getValue();
        String service = microserviceChoice.getValue();
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "copy C:\\Calculadora\\Microservicios\\" + service + ".jar C:\\Calculadora\\Server" + server);
        builder.start();
        serverChoice.setValue("");
        microserviceChoice.setValue("");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            adminSocket = new ServerSocket(port);
            Package temp = new Package('A', port);
            temp.setCode(0);
            sendPackage(temp);
            receivePackage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        microserviceChoice.getItems().addAll("Suma", "Resta", "Mult", "Div");
    }

    static void sendPackage(Package packageToClient) {
        try {
            packageToClient.setPackageEmisor('A');
            packageToClient.setLastEmisor('A');
            packageToClient.setEmisor(port);
            Socket socketSender = new Socket("localhost", nodePort);
            ObjectOutputStream outputStream = new ObjectOutputStream(socketSender.getOutputStream());
            outputStream.writeObject(packageToClient);
            socketSender.close();
        } catch (Exception ignored) {
            nodePort++;
            if (nodePort == 5020)
                nodePort = 5000;
        }
    }

    void receivePackage() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = adminSocket.accept();
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                    Package serverPackage = (Package) inputStream.readObject();
                    if (serverPackage.getPackageEmisor() == 'S') {
                        servers.add(String.valueOf(serverPackage.getEmisor()));
                        Platform.runLater(() -> {
                            serverChoice.getItems().clear();
                            serverChoice.getItems().addAll(servers);
                        });
                    }
                    inputStream.close();
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void closeAdmin() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }
}
