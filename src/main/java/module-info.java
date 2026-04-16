module org.igo.threads {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.igo.threads to javafx.fxml;
    exports org.igo.threads;
}