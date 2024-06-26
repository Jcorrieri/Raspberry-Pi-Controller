module com.example.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.hierynomus.sshj;

    opens com.example.app to javafx.fxml;
    exports com.example.app;
    exports com.example.app.Controllers;
    opens com.example.app.Controllers to javafx.fxml;
    exports com.example.app.IO;
    opens com.example.app.IO to javafx.fxml;
    exports com.example.app.Testing;
    opens com.example.app.Testing to javafx.fxml;
}