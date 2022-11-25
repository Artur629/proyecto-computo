package com.example.calcufinal;

import java.io.Serializable;
public class Package implements Serializable {
    private int operationCode;
    private int emisor;
    private double num1;
    private double num2;
    private double result;
    private char lastEmisor;
    private char packageEmisor;
    private String event;
    private String footprint;
    private boolean valid;
    private boolean proccesedByServer;
    private int originalEmisor;
    private int clonePort;
    private int acusesSuma;
    private int acusesResta;
    private int acusesMult;
    private int acusesDiv;

    public Package(char packageEmisor, int emisor) {
        this.packageEmisor = packageEmisor;
        this.emisor = emisor;
    }

    public void setPackageEmisor(char packageEmisor) { this.packageEmisor = packageEmisor; }
    public char getPackageEmisor() { return packageEmisor; }
    public void setAcuses(int acusesSuma, int acusesResta, int acusesMult, int acusesDiv) {
        this.acusesSuma = acusesSuma;
        this.acusesResta = acusesResta;
        this.acusesMult = acusesMult;
        this.acusesDiv = acusesDiv;
    }
    public int getAcusesSuma() { return acusesSuma; }
    public int getAcusesResta() { return acusesResta; }
    public int getAcusesMult() { return acusesMult; }
    public int getAcusesDiv() { return acusesDiv; }

    public void setCode(int operationCode) { this.operationCode = operationCode; }
    public int getCode() { return operationCode; }

    public void setEmisor(int emisor) { this.emisor = emisor; }
    public int getEmisor() {return emisor;}

    public void setLastEmisor(char lastEmisor) { this.lastEmisor = lastEmisor; }
    public char getLastEmisor() { return lastEmisor; }
    public void setOriginalEmisor(int originalEmisor) { this.originalEmisor = originalEmisor; }
    public int getOriginalEmisor() { return originalEmisor; }

    public void setClonePort(int clonePort) { this.clonePort = clonePort; }
    public int getClonePort() { return clonePort; }

    public void setNum1(double num1) { this.num1 = num1; }
    public double getNum1() {return num1;}

    public void setNum2(double num2) { this.num2 = num2; }
    public double getNum2() {return num2;}
    public void setFP(String footprint) { this.footprint = footprint; }
    public String getFP() { return footprint; }
    public void setValidOp(boolean valid) {this.valid = valid; }
    public boolean isValidOp() {return valid; }
    public void setResult(double result) { this.result = result; }
    public double getResult() {return result; }

    public void setEvent(String event) {this.event = event; }
    public String getEvent() {return event; }

    public void setProccesedByServer(boolean proccesedByServer) { this.proccesedByServer = proccesedByServer; }
    public boolean isProccesedByServer() { return proccesedByServer; }

}
