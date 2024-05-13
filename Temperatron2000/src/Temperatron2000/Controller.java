package Temperatron2000;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.File;

import com.fazecast.jSerialComm.SerialPort;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Questa classe si occupa di ricevere i comandi dell'utente (attraverso l'interfaccia grafica)
 * e manipolarli per poi adattare di conseguenza i componenti mostrati e il funzionamento delle 
 * classi dipendenti. Si basa sul modello MVC, modello-vista-controllo, dove questa classe rappresenta
 * l'ultimo componente del modello. Per ulteriori informazioni su questo tipo di approccio alla
 * programmazione vedere il link qui sotto.
 * 
 * E' importante impostare questa classe come Controller nel file XML dell'interfaccia grafica
 * 
 * @see <a href="https://it.wikipedia.org/wiki/Model-view-controller">MVC: modello-vista-controllo</a> 
 */
public class Controller {
	
	// allocazione di ogni componente dell'interfaccia grafica a cui si vuole accedere
    @FXML
    TableColumn<Record, String> tcID;

    @FXML
    TableColumn<Record, String> tcTemperatura;

    @FXML
    TableColumn<Record, String> tcData;

    @FXML
    TableColumn<Record, String> tcOra;

    @FXML 
    TableView<Record> tblLetture;

    @FXML
    ComboBox<String> cbPorte;

    @FXML
    Slider slTemperatura;

    @FXML
    ComboBox<String> cbSelezioneTempo;

    @FXML
    TextField tfTempo;

    @FXML
    Button btAvvioTimer;

    @FXML
    Label lbProssimaLettura;

    @FXML
    Label lbUltimaLettura;

    @FXML
    Label lbStatoPorta;
    
    // allocazione degli stati di operazione
    private boolean ripetizione;
    private boolean connessione;
    
    // allocazione dei gestori esterni
    private GestoreSR gestoreSR;
    private GestoreDB gestoreDB;
    
    // allocazione degli elementi comuni tra le funzioni della classe
    private Map<String, String> porte;
    private Orologio orologio;
    private Stage stage;

    /**
     * Funzione di inizializzazione delle varie componenti dell'interfaccia grafica e delle
     * variabili del programma. Questa funzione viene eseguita prima del rendering della finestra.
     */
    @FXML 
    public void initialize() { 
        ripetizione = false;
        connessione = false;
        
        // inizializzazione dei gestori esterni alla classe
        gestoreSR = new GestoreSR();
        gestoreDB = new GestoreDB("jdbc:sqlite:database/lett_temp.db", "temperature", this);
        
        // funzioni di assegnazione e popolazione della TableView contenente i dati del database
        tcID.setCellValueFactory(new PropertyValueFactory<Record,String>("ID"));
        tcTemperatura.setCellValueFactory(new PropertyValueFactory<Record,String>("temperatura"));
        tcData.setCellValueFactory(new PropertyValueFactory<Record,String>("data"));
        tcOra.setCellValueFactory(new PropertyValueFactory<Record,String>("ora"));

        tblLetture.setRowFactory(tv -> {
            TableRow<Record> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (! row.isEmpty() && event.getButton()==MouseButton.PRIMARY 
                     && event.getClickCount() == 1) {
        
                    Record clickedRow = row.getItem();
                    aggiornaTermometro(clickedRow.getTemperatura());
                }
            });
            return row ;
        });
        
        // popolazione della ComboBox per la selezione delle unità di tempo
        String[] misureDiTempo = {"secondi", "minuti", "ore"};
        cbSelezioneTempo.getItems().clear();
        cbSelezioneTempo.getItems().addAll(misureDiTempo);
        cbSelezioneTempo.getSelectionModel().selectFirst();
    }
    
    public void impostaStage(Stage stage) {
    	this.stage = stage;
    }

    /**
     * Questa funzione viene eseguita al rendering della finestra, cioè nonappena sarà
     * visibile sullo schermo
     */
    public void eseguiAlrender() {
        aggiornaPorte();
        aggiornaTabella();
    }

    /**
     * Questa funzione mostra una notifica. Se il messaggio corrisponde a GestoreSR.CONNESSIONE_ER
     * l'informazione della notifica viene descritta con un generico messaggio d'errore seriale.
     * 
     * @param tipo Tipologia della funzione espressa con AlertType
     * @param messaggio Stringa che descrive il messaggio da inserire nella notifica
     * @param header Stringa che descrive l'header della notifica
     */
    public void mostraNotifica(AlertType tipo, String messaggio, String header) {
        Alert a = new Alert(tipo);
        
        a.setTitle("Temperatron2000");
        a.setHeaderText(header);

        switch (messaggio) {
            case GestoreSR.CONNESSIONE_ER:
                a.setContentText("Non è stato possibile connettersi alla porta");
                break;
        
            default:
                a.setContentText(messaggio);
                break;
        }
        
        a.show();
    }
    
    /**
     * Questa funzione viene chiamata dal relativo pulsante nell'interfaccia grafica e si 
     * divide in due parti: esportaCSV() in Controller e scritturaRecordsCSV(File) in GestoreDB.
     * 
     * In questa prima parte costruisce il titolo del file, lancia il pannello di salvataggio
     * tipico del sistema operativo, e in caso di avvenuta selezione del file e del percorso, 
     * chiama la funzione scritturaRecordsCSV(File) passando la directory del file selezionato
     * per l'esportazione
     */
    @FXML
    public void esportaCSV() {
    	SimpleDateFormat formattatoreData = new SimpleDateFormat("dd_MM_yyyy");
        String data = formattatoreData.format(new Date());
        
    	FileChooser selezionaFile = new FileChooser();
    	selezionaFile.setInitialFileName("temperature_" + data + ".csv");
    	selezionaFile.setTitle("Esporta CSV");
    	
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("File CSV (*.csv)", "*.csv");
        selezionaFile.getExtensionFilters().add(extFilter);
        
        File file = selezionaFile.showSaveDialog(stage);
        
        if (file != null) {
        	
        	gestoreDB.scritturaRecordsCSV(file, GestoreDB.DEFAULT_HEADERS);
        }
        
    }

    /**
     * Funzione che inizia la procedura di verifica della connessione con Arduino.
     * Viene chiamata dal pulsante di connessione indicato sull'interfaccia grafica
     */
    @FXML
    public void connettiAdArduino() {
    	
    	// costruisce un oggetto SerialPort indicando la porta selezionata con il valore scelto nella ComboBox
        SerialPort porta = SerialPort.getCommPort(porte.get(cbPorte.getValue()));

        try {
            lbStatoPorta.setText("Stato: connessione...");
            gestoreSR.inizializzaPorta(porta, this);
            gestoreSR.scrittura(GestoreSR.CONNESSIONE);
            connessione = false;

        } catch (Exception e) {
            lbStatoPorta.setText("Stato: non connesso");
            mostraNotifica(AlertType.ERROR, GestoreSR.CONNESSIONE_ER, "Connessione serial");
        }
        
    }

    /**
     * Funzione che avvia il conto alla rovescia del timer. Viene chiamata dal pulsante
     * di avvio del tempo nell'interfaccia grafica. Si occupa di convertire in secondi 
     * l'input a prescindere dall'unità indicata, di adattare le funzioni delle componenti
     * grafiche in base allo stato del timer e di assicurarsi che la connessione con Arduino
     * sia verificata prima di iniziare l'operazione
     */
    @FXML
    public void avviaTimer() {

        if (!tfTempo.getText().trim().isEmpty()) {

            long tempoEffettivo = 0;
            long inputTempo = Long.parseLong(tfTempo.getText().trim());

            if (inputTempo <= 60) {
                switch (cbSelezioneTempo.getValue()) {
                    case "secondi":
                        tempoEffettivo = inputTempo * 1;
                        break;
    
                    case "minuti":
                        tempoEffettivo = inputTempo * 60;
                        break;
    
                    case "ore":
                        tempoEffettivo = inputTempo * 3600;
                        break;
                
                    default:
                        break;
                }
                
                // avvio del conto alla rovescia vero e proprio
                orologio = new Orologio(tempoEffettivo, this);
                orologio.avvia();
                ripetizione = true;

                btAvvioTimer.setTextFill(Color.web("#cd0000"));
                btAvvioTimer.setText("Interrompi");
                btAvvioTimer.setOnAction((e) -> {
                    if (ripetizione) {
                        interrompiTimer(orologio);
                    }
                });
            }
            
        } else if (!connessione) {
            mostraNotifica(AlertType.INFORMATION, "Per poter avviare il ciclo di lettura è prima necessario connettersi al dispositivo", "Connessione seriale");
        }
        
    }
    
    /**
     * Interrompe il conto alla rovescia e ripristina le componenti dell'interfaccia
     * grafica allo stato precedente all'avvio del timer. Questa funzione viene chiamata
     * dal pulsante di arresto che compare nell'interfaccia grafica solo nel 
     * periodo di esecuzione del timer
     * 
     * @param orologio Oggetto di tipo Orologio da interrompere
     */
    private void interrompiTimer(Orologio orologio) {
        orologio.arresta(); // interruzione del timer vera e propria

        ripetizione = false;
        lbProssimaLettura.setText("");

        btAvvioTimer.setOnAction((e) -> {
            if (!ripetizione) {
                avviaTimer();
            }
        });

        btAvvioTimer.setTextFill(Color.BLACK);
        btAvvioTimer.setText("Avvia");
    }

    /**
     * Funzione di callback che viene chiamata dalla classe Orologio ogni qualvolta che 
     * passa un secondo. Si occupa di convertire il tempo nelle varie unità di tempo che
     * compongono il tempo totale (secondi, minuti, ore) e di rappresentarlo volta per 
     * volta nell'interfaccia grafica
     * 
     * @param durata Tempo rimanente in secondi espresso in formato Long 
     */
    public void aggiornaTempoRimanente(long durata) {
        switch (cbSelezioneTempo.getValue()) {
            case "secondi":
                lbProssimaLettura.setText("Prossima lettura tra " + (durata % 60) + " secondi");
                break;

            case "minuti":
                lbProssimaLettura.setText("Prossima lettura tra " + ((durata % 3600) / 60) + " minuti e " + (durata % 60) + " secondi");
                break;

            case "ore":
                lbProssimaLettura.setText("Prossima lettura tra " + (durata / 3600) + " ore, " + ((durata % 3600) / 60) + " minuti e " + (durata % 60) + " secondi");
                break;
        
            default:
                break;
        }
    }

    /**
     * Funzione di callback che viene chiamata dalla classe Orologio quando il conto alla rovescia
     * ha raggiunto la fine. Se il timer non è stato precedentemente interrotto allora il ciclo
     * di conto alla rovescia riprende da capo. In questa funzione inzierà la richiesta di dati 
     * verso arduino con il comando apposito GestoreSR.LETTURA
     */
    public void tempoEsaurito() {
        lbProssimaLettura.setText("");

        SimpleDateFormat formattatore = new SimpleDateFormat("HH:mm");
        String data = formattatore.format(new Date());

        lbUltimaLettura.setText("Ultima lettura alle " + data);

        gestoreSR.scrittura(GestoreSR.LETTURA); // scrittura del comando di richiesta ad Arduino

        if (ripetizione) {
            avviaTimer();
        }
    }

    /**
     * Funzione di callback che viene chiamata dalla classe GestoreSR per restituire l'esito
     * o la risposta che ha fornito la task seriale. In caso di sola verifica delle connessione,
     * cioè inviando il comando GestoreSR.CONNESSIONE, si potrà ricevere in risposta o 
     * GestoreSR.CONNESSIONE_OK o GestoreSR.CONNESSIONE_ER. Se la risposta differisce dalle ultime 
     * due menzionate, viene considerata come lettura e viene scritta nel database.
     * 
     * @param risultato Stringa che rappresenta il risultato della task seriale
     */
    public void risultatoTaskSeriale(String risultato) {
        if (risultato.equals(GestoreSR.CONNESSIONE_OK)) {
            lbStatoPorta.setText("Stato: connesso");
            connessione = true;

        } else if (risultato.equals(GestoreSR.CONNESSIONE_ER)) {
            mostraNotifica(AlertType.ERROR, GestoreSR.CONNESSIONE_ER, "Connessione seriale");
            lbStatoPorta.setText("Stato: non connesso");

            connessione = false;
            ripetizione = false;
            interrompiTimer(orologio);
        } else {
        	
        	/* per una risposta personalizzata a un comando personalizzato, aggiungere un
        	 * controllo sul tipo di risposta come fatto qua sopra, altrimenti la Stringa verrà 
        	 * considerata come valore di lettura a prescindere e potrebbe generare errori nella
        	 * coversione in tipo Float (se non è di natura numerica)
        	 */
        	
        	System.out.println("lettura rilevata: " + risultato);
        	
        	SimpleDateFormat formattatoreOra = new SimpleDateFormat("HH:mm:ss");
            String ora = formattatoreOra.format(new Date());
            
            SimpleDateFormat formattatoreData = new SimpleDateFormat("dd/MM/yyyy");
            String data = formattatoreData.format(new Date());
        	
        	Record record = new Record(0, Float.parseFloat(risultato), data, ora);
        	
        	gestoreDB.scritturaRecord(record); // aggiunta dell'oggetto Record appena costruito nel database
        	aggiornaTabella();
        	
        }
    }

    /**
     * Questa funzione aggiorna la lista delle porte seriali disponibili e di conseguenza
     * popola la ComboBox di selezione delle porte. Questa funzione è asincrona e l'ottenimento
     * della lista è affidato a una sottofunzione privata caricaPorte()
     */
    public void aggiornaPorte() {
        
        Task<Map<String, String>> task_porta = new Task<Map<String, String>>() { 
            @Override 
            protected Map<String, String> call() throws Exception { 
                return caricaPorte(); 
            } 
 
            @Override 
            protected void succeeded() {
                porte = getValue();
                cbPorte.getItems().clear(); 
                cbPorte.getItems().addAll(porte.keySet());
                cbPorte.getSelectionModel().selectFirst();;
            } 
        }; 
 
        new Thread(task_porta).start(); 

    }

    /**
     * Questa funzione aggiorna la tabella TableView in modo asincrono e fa affidamento
     * alla funzione privata popolaTabella() per l'ottenimento degli elementi da
     * inserire
     */
    public void aggiornaTabella() {

        Task<List<Record>> task_db = new Task<List<Record>>() { 
            @Override 
            protected List<Record> call() throws Exception { 
                return caricaRecords(); 
            } 
 
            @Override 
            protected void succeeded() { 
                tblLetture.getItems().clear(); 
                tblLetture.getItems().addAll( getValue() ); 
            } 
        }; 
 
        new Thread(task_db).start(); 
    }

    /**
     * Questa funzione imposta il valore della temperatura sullo slider che rappresenta il 
     * termometro
     * 
     * @param temperatura Temperatura espressa in Float, da -10 a +40
     */
    private void aggiornaTermometro(float temperatura) {
        slTemperatura.setValue(temperatura);
    }

    /**
     * Questa funzione ritorna una lista statica di elementi Record estratti dal database
     * 
     * @return Una List di Record con gli elementi del database
     */
    private List<Record> caricaRecords() { 
        List<Record> records = gestoreDB.letturaRecords(); 
 
        return records; 
    }

    /**
     * Questa funzione ritorna le informazioni delle porte seriali connesse e disponibili
     * 
     * @return Una matrice [String, String] con i i nomi di sistema e le descrizioni delle porte 
     * 		seriali connesse al computer
     */
    private Map<String, String> caricaPorte() {
        Map<String, String> porte = gestoreSR.ottieniPorte();

        return porte;
    }
}
