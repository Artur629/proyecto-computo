package com.example.calcufinal;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClientController implements Initializable {

    private static ServerSocket ss;
    private static int port = 6000;
    private static String res = "";
    private static int nodePort = 5000;
    private static String footprint = "";
    public Button closeButton;
    private double num1;
    private String op = "";
    private boolean start = true;
    private boolean decimal = false;

    private static final ArrayList<Package> listSuma =  new ArrayList<>();
    private static final ArrayList<Package> listResta = new ArrayList<>();
    private static final ArrayList<Package> listMult = new ArrayList<>();
    private static final ArrayList<Package> listDiv = new ArrayList<>();
    private static final Set<String> numAcusesSuma = new HashSet<>();
    private static final Set<String> numAcusesResta = new HashSet<>();
    private static final Set<String> numAcusesMult = new HashSet<>();
    private static final Set<String> numAcusesDiv = new HashSet<>();
    private static final Set<String> ongoingEvents = new HashSet<>();
    private static final Set<String> processedEvents = new HashSet<>();
    private static int lastMinSum = 0;
    private static int lastMinResta = 0;
    private static int lastMinMult = 0;
    private static int lastMinDiv = 0;
    private static int minSum = 1;
    private static int minRes = 1;
    private static int minMult = 1;
    private static int minDiv = 1;

    @FXML
    private Label resOut;

    @FXML
    private TextArea clientLog;

    @FXML
    private void clearOutput() {
        Platform.runLater(() -> {
            decimal = false;
            resOut.setText("0.0");
            start = true;
            op = "";
        });
    }

    @FXML
    private void clearLog() {
        clientLog.setText("");
    }

    @FXML
    private void numPad(ActionEvent event) {
        String value = ((Button)event.getSource()).getText();
        if (!decimal || !value.equals(".")) {
            if (start) {
                resOut.setText("");
                start = false;
            }
            if (value.equals(".")) {
                decimal = true;
            }
            resOut.setText(resOut.getText() + value);
        }
    }

    @FXML
    private void Operator(ActionEvent event) {
        decimal = false;
        if (resOut.getText().equals("Error"))
            return;
        String value = ((Button)event.getSource()).getText();
        if (!value.equals("=")) {
            if (!op.isEmpty()) {
                return;
            }
            op = value;
            num1 = Double.parseDouble(resOut.getText());
            resOut.setText("");
        } else {
            if (op.isEmpty()) { 
                return;
            }
            if (resOut.getText().isEmpty() || resOut.getText().equals(".") || "Error".equals(String.valueOf(num1))) {
                resOut.setText("Error");
                op = "";
                start = true;
                return;
            }
            calculateOperation(num1, Double.parseDouble(resOut.getText()), op);
            op = "";
            start = true;
        }
    }

    private void initializeClients() {
        while (true) {
            try {
                ss = new ServerSocket(port);
                footprint = String.valueOf(port);
                sendPackage(new Package('C', port));
                break;
            } catch (Exception ex) {
                port++;
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeClients();
        System.out.println("Port: " + port);
        receivePackage();
    }

    void receivePackage() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = ss.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Package serverPackage = (Package) ois.readObject();
                    if (serverPackage.getPackageEmisor() == 'S' && serverPackage.getCode() != 0 && serverPackage.getOriginalEmisor() == port) {
                        if (serverPackage.isProccesedByServer() && !processedEvents.contains(serverPackage.getEvent())) {
                            processedEvents.add(serverPackage.getEvent());
                            res = String.valueOf(serverPackage.getResult());
                            String symbol = switch (serverPackage.getCode()) {
                                case 1 -> "+";
                                case 2 -> "-";
                                case 3 -> "*";
                                case 4 -> "/";
                                default -> "";
                            };
                            Platform.runLater(() -> {
                                resOut.setText(res);
                                clientLog.appendText("Operation: " + serverPackage.getCode() + "\n");
                                clientLog.appendText(serverPackage.getNum1() + " " + symbol + " " + serverPackage.getNum2() + " = " + serverPackage.getResult() + "\n\n");
                            });
                        } else {
                            appendFP(serverPackage);
                            checkAcuse(serverPackage);
                        }
                    } else if (serverPackage.getPackageEmisor() == 'A') {
                        minSum = serverPackage.getAcusesSuma();
                        minRes = serverPackage.getAcusesResta();
                        minMult = serverPackage.getAcusesMult();
                        minDiv = serverPackage.getAcusesDiv();
                    }
                    ois.close();
                    socket.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private static void operation(int operationCode) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        switch (operationCode) {
            case 1 -> {
                for (Package packageToServer : listSuma) {
                    packageToServer.setValidOp(true);
                    sendPackage(packageToServer);
                }
                listSuma.clear();
            }
            case 2 -> {
                for (Package packageToServer : listResta) {
                    packageToServer.setValidOp(true);
                    sendPackage(packageToServer);
                }
                listResta.clear();
            }
            case 3 -> {
                for (Package packageToServer : listMult) {
                    packageToServer.setValidOp(true);
                    sendPackage(packageToServer);
                }
                listMult.clear();
            }
            case 4 -> {
                for (Package packageToServer : listDiv) {
                    packageToServer.setValidOp(true);
                    sendPackage(packageToServer);
                }
                listDiv.clear();
            }
        }
    }

    private static void calculateOperation(double num1, double num2, String op) {
        numAcusesSuma.clear();
        numAcusesResta.clear();
        numAcusesMult.clear();
        numAcusesDiv.clear();

        Package packageToServer = new Package('C', port);

        packageToServer.setNum1(num1);
        packageToServer.setNum2(num2);
        packageToServer.setEvent(generateSHA(System.currentTimeMillis() + footprint));
        packageToServer.setValidOp(false);
        packageToServer.setOriginalEmisor(port);
        packageToServer.setClonePort(0);

        switch (op) {
            case "+" -> packageToServer.setCode(1);
            case "-" -> packageToServer.setCode(2);
            case "*" -> packageToServer.setCode(3);
            case "/" -> packageToServer.setCode(4);
        }

        switch (packageToServer.getCode()) {
            case 1 -> listSuma.add(packageToServer);
            case 2 -> listResta.add(packageToServer);
            case 3 -> listMult.add(packageToServer);
            case 4 -> listDiv.add(packageToServer);
        }

        sendPackage(packageToServer);
    }

    static void sendPackage(Package packageToServer) {
        new Thread(() -> {
            while (true) {
                try {
                    packageToServer.setLastEmisor('C');
                    packageToServer.setFP(footprint);
                    packageToServer.setPackageEmisor('C');
                    packageToServer.setEmisor(port);
                    Socket socket = new Socket("localhost", nodePort);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(packageToServer);
                    oos.close();
                    socket.close();
                    break;
                } catch (Exception e) {
                    nodePort++;
                    if (nodePort == 5020)
                        nodePort = 5000;
                }
            }
        }).start();
    }

    public static String generateSHA(String input) {
        String sha1="";
        String value= String.valueOf(input);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e){
            e.printStackTrace();
        }
        return sha1;
    }

    private static void appendFP(Package serverPackage) {
        switch (serverPackage.getCode()) {
            case 1 -> numAcusesSuma.add(serverPackage.getFP());
            case 2 -> numAcusesResta.add(serverPackage.getFP());
            case 3 -> numAcusesMult.add(serverPackage.getFP());
            case 4 -> numAcusesDiv.add(serverPackage.getFP());
        }
    }

    private static int cloneServer(Set<String> acuses) {
        int valorMin = 65535;
        for (String acuse : acuses) {
            if (Integer.parseInt(acuse) < valorMin)
                valorMin = Integer.parseInt(acuse);
        }
        return valorMin;
    }

    private static void checkAcuse(Package serverPackage) {
        int sleepTime = 3;

        if (ongoingEvents.contains(serverPackage.getEvent()))
            return;
        ongoingEvents.add(serverPackage.getEvent());
        new Thread(() -> {
            try {
                switch (serverPackage.getCode()) {
                    case 1 -> {
                        while (numAcusesSuma.size() < minSum) {
                            if (lastMinSum == numAcusesSuma.size()) {
                                Package cloneServer = new Package('C', port);
                                cloneServer.setClonePort(cloneServer(numAcusesSuma));
                                cloneServer.setCode(1);
                                sendPackage(cloneServer);
                                if (lastMinSum + 1 == minSum)
                                    break;
                            }
                            lastMinSum = numAcusesSuma.size();
                            TimeUnit.SECONDS.sleep(sleepTime);
                            sendPackage(serverPackage);
                        }
                        lastMinSum = 0;
                        operation(1);
                    }
                    case 2 -> {
                        while (numAcusesResta.size() < minRes) {
                            if (lastMinResta == numAcusesResta.size()) {
                                Package cloneServer = new Package('C', port);
                                cloneServer.setClonePort(cloneServer(numAcusesResta));
                                cloneServer.setCode(2);
                                sendPackage(cloneServer);
                                if (lastMinResta + 1 == minRes)
                                    break;
                            }
                            lastMinResta = numAcusesResta.size();
                            TimeUnit.SECONDS.sleep(sleepTime);
                            sendPackage(serverPackage);
                        }
                        lastMinResta = 0;
                        operation(2);
                    }
                    case 3 -> {
                        while (numAcusesMult.size() < minMult) {
                            if (lastMinMult == numAcusesMult.size()) {
                                Package cloneServer = new Package('C', port);
                                cloneServer.setClonePort(cloneServer(numAcusesMult));
                                cloneServer.setCode(3);
                                sendPackage(cloneServer);
                                if (lastMinMult + 1 == minMult)
                                    break;
                            }
                            lastMinMult = numAcusesMult.size();
                            TimeUnit.SECONDS.sleep(sleepTime);
                            sendPackage(serverPackage);
                        }
                        lastMinMult = 0;
                        operation(3);
                    }
                    case 4 -> {
                        while (numAcusesDiv.size() < minDiv) {
                            if (lastMinDiv == numAcusesDiv.size()) {
                                Package cloneServer = new Package('C', port);
                                cloneServer.setClonePort(cloneServer(numAcusesDiv));
                                cloneServer.setCode(4);
                                sendPackage(cloneServer);
                                if (lastMinDiv + 1 == minDiv)
                                    break;
                            }
                            lastMinDiv = numAcusesDiv.size();
                            TimeUnit.SECONDS.sleep(sleepTime);
                            sendPackage(serverPackage);
                        }
                        lastMinDiv = 0;
                        operation(4);
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    public void closeClient() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }
}
