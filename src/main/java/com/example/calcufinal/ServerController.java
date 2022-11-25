package com.example.calcufinal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

public class ServerController implements Initializable {

    private static ServerSocket ss;
    private static int port = 7000;
    private static int nodePort = 5000;
    private static String footprint = "";
    private static final Set<String> events = new HashSet<>();
    private static int serverToClone = 0;
    @FXML
    public Label serverPort;
    @FXML
    public Button closeButton;
    @FXML
    private TextArea serverLog;

    @FXML
    void clearLog() {
        serverLog.setText("");
    }
    


    void receivePackage() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = ss.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Package clientData = (Package) ois.readObject();
                    if (clientData.getPackageEmisor() == 'C' && checkService(clientData.getCode())) {
                        if (clientData.isValidOp()) {
                            if (!events.contains(clientData.getEvent()))
                                solveOp(clientData);
                            else
                                continue;
                        }
                        else {
                            if (clientData.getClonePort() == port && (serverToClone == clientData.getEmisor() || serverToClone == 0)) {
                                serverToClone = clientData.getEmisor();
                                cloneServer();
                            }
                            else
                                sendProcessedData(clientData);
                        }
                    }
                    ois.close();
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private boolean checkService(int serviceCode) {
        String serviceName = switch (serviceCode) {
            case 1 -> "Suma.jar";
            case 2 -> "Resta.jar";
            case 3 -> "Mult.jar";
            case 4 -> "Div.jar";
            default -> "";
        };

        File folder = new File("C:\\Calculadora\\Server" + port);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(serviceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void cloneServer() throws IOException {
        ss.close();
        int checkPort = port;
        int newPort = 7000;
        ServerSocket newServer;
        while (true) {
            try {
                if (checkPort != newPort) {
                    newServer = new ServerSocket(newPort);
                    break;
                } else {
                    newPort++;
                }
            } catch (Exception e) {
                newPort++;
            }
        }
        newServer.close();

        new ProcessBuilder("cmd.exe", "/c", "del /S /Q C:\\Calculadora\\Server" + newPort + "\\*").start();
        new ProcessBuilder("cmd.exe", "/c", "xcopy C:\\Calculadora\\Server" + port + " C:\\Calculadora\\Server" + newPort + " /Y").start();
        //D:\JavaProjects\CalcuFinal\out\artifacts\CalcuFinal_jar\CalcuFinal.jar
        ss = new ServerSocket(port);
        new ProcessBuilder("cmd.exe", "/c", "java -jar D:\\JavaProjects\\CalcuFinal\\out\\artifacts\\Server_jar\\CalcuFinal.jar").start();
    }

    void solveOp(Package receivedPackage) {
        serverToClone = 0;
        double num1 = receivedPackage.getNum1();
        double num2 = receivedPackage.getNum2();
        int op = receivedPackage.getCode();
        double result;

        try {
            result = switch (op) {
                case 1 -> serviceSuma(num1, num2);
                case 2 -> serviceResta(num1, num2);
                case 3 -> serviceMult(num1, num2);
                case 4 -> serviceDiv(num1, num2);
                default -> 0;
            };
        } catch (Exception e) {
            receivedPackage.setProccesedByServer(false);
            return;
        }

        String operator = switch (op) {
            case 1 -> "+";
            case 2 -> "-";
            case 3 -> "*";
            case 4 -> "/";
            default -> "";
        };

        double opRes = result;
        Platform.runLater(() -> {
            serverLog.appendText("Operation: " + op + "\n");
            serverLog.appendText(num1 + " " + operator + " " + num2 + " = " + opRes + "\n\n");
        });

        receivedPackage.setResult(result);
        receivedPackage.setProccesedByServer(true);
        events.add(receivedPackage.getEvent());
        sendProcessedData(receivedPackage);
    }

    private double serviceSuma(double num1, double num2) throws Exception {
        double result;
        File dir = new File("C:\\Calculadora\\Server" + port + "\\Suma.jar");
        Class<?> cls = new URLClassLoader(new URL[] { dir.toURI().toURL() }).loadClass("Suma");
        Method suma = cls.getMethod("sumar", double.class, double.class);
        Object objInstance = cls.getDeclaredConstructor().newInstance();
        result = (double)suma.invoke(objInstance, num1, num2);
        return result;
    }

    private double serviceResta(double num1, double num2) throws Exception {
        double result;
        File dir = new File("C:\\Calculadora\\Server" + port + "\\Resta.jar");
        Class<?> cls = new URLClassLoader(new URL[] { dir.toURI().toURL() }).loadClass("Resta");
        Method resta = cls.getMethod("restar", double.class, double.class);
        Object objInstance = cls.getDeclaredConstructor().newInstance();
        result = (double)resta.invoke(objInstance, num1, num2);
        return result;
    }

    private double serviceMult(double num1, double num2) throws Exception {
        double result;
        File dir = new File("C:\\Calculadora\\Server" + port + "\\Mult.jar");
        Class<?> cls = new URLClassLoader(new URL[] { dir.toURI().toURL() }).loadClass("Mult");
        Method mult = cls.getMethod("multi", double.class, double.class);
        Object objInstance = cls.getDeclaredConstructor().newInstance();
        result = (double)mult.invoke(objInstance, num1, num2);
        return result;
    }

    private double serviceDiv(double num1, double num2) throws Exception {
        double result;
        File dir = new File("C:\\Calculadora\\Server" + port + "\\Div.jar");
        Class<?> cls = new URLClassLoader(new URL[] { dir.toURI().toURL() }).loadClass("Div");
        Method div = cls.getMethod("division", double.class, double.class);
        Object objInstance = cls.getDeclaredConstructor().newInstance();
        result = (double)div.invoke(objInstance, num1, num2);
        return result;
    }

    static void sendProcessedData(Package packageToClient) {
        new Thread(() -> {
            while (true) {
                try {
                    packageToClient.setPackageEmisor('S');
                    packageToClient.setLastEmisor('S');
                    packageToClient.setEmisor(port);
                    packageToClient.setFP(footprint);
                    Socket socketSender = new Socket("localhost", nodePort);
                    ObjectOutputStream outputStream = new ObjectOutputStream(socketSender.getOutputStream());
                    outputStream.writeObject(packageToClient);
                    socketSender.close();
                    break;
                } catch (Exception ignored) {
                    nodePort++;
                    if (nodePort == 5020)
                        nodePort = 5000;
                }
            }
        }).start();
    }

    void initializeServers() {
        while (true) {
            try {
                ss = new ServerSocket(port);
                footprint = String.valueOf(port);
                Package temp = new Package('S', port);
                Platform.runLater(() -> serverPort.setText("Port: " + port));
                temp.setCode(0);
                sendProcessedData(temp);
                break;
            } catch (Exception ex) {
                port++;
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeServers();
        System.out.println("Port: " + port);
        receivePackage();
    }

    public void closeServer() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }
}
