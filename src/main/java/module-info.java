module com.example.calcufinal {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calcufinal to javafx.fxml;
    exports com.example.calcufinal;
}