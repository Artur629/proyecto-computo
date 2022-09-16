module com.example.calcucomputo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calcucomputo to javafx.fxml;
    exports com.example.calcucomputo;
}