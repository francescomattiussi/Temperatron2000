package Temperatron2000;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione, prima parte del modello MVC, cioè il model. In questa classe
 * si si crea la finestra, le si associa la consfigurazione grafica e si chiamano le funzioni di 
 * inizializzazione dichiarate nel Controller. In questa classe si controlla l'esecuzione in sé del
 * processo del programma e del thread grafico di JavaFX.
 * 
 * La seconda componente del modello MVC, cioè la View, in questo caso è composta da un file XML
 * contenente l'indicazione dei singoli componenti con le loro rispettive proprietà. Questo file è
 * stato composto su Scene Builder e generato dallo stesso.
 * 
 * Per ulteriori informazioni su questo tipo di approccio alla programmazione vedere il link qui sotto.
 * 
 * @see <a href="https://it.wikipedia.org/wiki/Model-view-controller">MVC: modello-vista-controllo</a>
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("temperatron_gui.fxml")); 
 
        Parent p = fxmlLoader.load(); // qui viene caricata l'interfaccia grafica nella finestra
 
        Controller controller = fxmlLoader.getController();
        
        // imposta le proprietà della finestra
        primaryStage.setTitle("Temperatron 2000 by Mattiussi & Guzzo");
        primaryStage.setScene(new Scene(p, 640, 570));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("./icone/robot.png")));
        
        // funzioni dichiarate nel Controller e da eseguire dopo il rendering
        primaryStage.setOnShown((windowEvent) -> {
            controller.eseguiAlrender();
            controller.impostaStage(primaryStage);
        });
        
        primaryStage.setResizable(false); // imposta la finestra come non ridimensionabile
        
        primaryStage.show(); // mostra la finestra
    }

    public static void main(String[] args) {
        launch(args); // il processo viene avviato
    }
}
