/// Автор: Игонин В.Ю.

package org.igo.threads;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ThreadsApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ThreadsApplication.class.getResource("Threads-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 650, 450);
        stage.setTitle("Вычисления в отдельном потоке");
        stage.setScene(scene);
        stage.show();
    }
}
