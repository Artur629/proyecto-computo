package com.example.calcufinal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class NodeController implements Initializable {

    private static ServerSocket nodeSocket;
    private static int port = 5000;
    private static final Set<Integer> children = new HashSet<>();
    private static final Set<Integer> nodes = new HashSet<>();

    @FXML
    public Label nodePort;
    @FXML
    public Button closeButton;
    @FXML
    private TextArea nodeLog;

    @FXML
    private void clearLog() {
        nodeLog.setText("");
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeMiddlewares();
        System.out.println("Port: " + port);
        receiveAndResendPackage();
    }

    private void initializeMiddlewares() {
        while (true) {
            try {
                nodeSocket = new ServerSocket(port);
                nodePort.setText("Port: " +  port);
                for (int port : nodes) {
                    Socket socket = new Socket("localhost", port);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(new Package('N', port));
                    socket.close();
                }
                break;
            } catch (Exception e) {
                nodes.add(port);
                port++;
            }
        }
    }

    void receiveAndResendPackage() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = nodeSocket.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Package data = (Package) ois.readObject();
                    socket.close();

                    if (data.getPackageEmisor() == 'N')
                        nodes.add(data.getEmisor());
                    else
                        children.add(data.getEmisor());


                    Platform.runLater(() -> {
                        nodeLog.appendText("Response from " + data.getEmisor() + "\t");
                        nodeLog.appendText("Operation: " + data.getCode() + "\n\n");
                    });


                    if (data.getLastEmisor() == 'N') { // Si viene de nodo, envías a las celulas conectadas
                        for (int child : children) {
                            if (child != data.getEmisor()) {
                                try {
                                    Socket childSocket = new Socket("localhost", child);
                                    ObjectOutputStream oos = new ObjectOutputStream(childSocket.getOutputStream());
                                    data.setLastEmisor('N');
                                    oos.writeObject(data);
                                    childSocket.close();
                                } catch (Exception ignored) {}
                            }
                        }
                    } else if (data.getPackageEmisor() != 'N') { // Si viene de celulas, envías a celulas y nodos
                        for (int node : nodes) {
                            if (node != data.getEmisor()) {
                                try {
                                    Socket childSocket = new Socket("localhost", node);
                                    ObjectOutputStream oos = new ObjectOutputStream(childSocket.getOutputStream());
                                    data.setLastEmisor('N');
                                    oos.writeObject(data);
                                    childSocket.close();
                                } catch (Exception ignored) {}
                            }
                        }
                        for (int child : children) {
                            if (child != data.getEmisor()) {
                                try {
                                    Socket childSocket = new Socket("localhost", child);
                                    ObjectOutputStream oos = new ObjectOutputStream(childSocket.getOutputStream());
                                    data.setLastEmisor('N');
                                    oos.writeObject(data);
                                    childSocket.close();
                                } catch (Exception ignored) {}
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void closeNode() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }
}