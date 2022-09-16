package com.example.calcucomputo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.*;

public class HelloController {
    @FXML
    private TextField txt_result;

    //metodo para hacer reset a la calculadora y ponerla en 0
    private boolean CisPressed (Button numero){
        if (numero.getText().equals("C")){
            return true;
        } else {
            return false;
        }
    }

    //metodo para que cuando se escriba un numero se quite el cero inicial
    @FXML
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
        num = txt_result.getText();
        Cliente clientObj = new Cliente();
        String resultado = clientObj.RecibirOperacion(txt_result.getText());
        txt_result.setText(resultado);
    }

}