package Temperatron2000;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Questa classe si occupa di gestire la comunicazione sull'interfaccia seriale
 */
public class GestoreSR {

    /**
     * Richiesta di connessione
     */
    public static final String CONNESSIONE = "connesso?";
    
    /**
     * Connessione avvenuta
     */
    public static final String CONNESSIONE_OK = "connesso";
    
    /**
     * Connessione fallita
     */
    public static final String CONNESSIONE_ER = "!connesso";

    /**
     * Richiesta della lettura
     */
    public static final String LETTURA = "lettura?";
    
    /**
     * Errore nella lettura
     */
    public static final String LETTURA_ER = "!lettura";

    private SerialPort portaSeriale;
    private Controller controller;

    /**
     * L'inizializzazione dei parametri base per il funzionamento non avviene nella dichiarazione della classe
     * ma in una funzione separata (inizializzazionePorta) per una maggiore flessibilità dei parametri in fase di esecuzione
     */
    public GestoreSR() {}

    /**
     * Questa funzione è necessario chiamarla prima di chiamare le altre presenti nella classe, essendo quella che
     * specifica la porta seriale da utilizzare. Specifica anche il Controller a cui agganciarsi, che conterrà la funzione
     * di callback risultatoTaskSeriale() dove le funzioni inizializzaPorta() e scrittura() potranno ritornare le informazioni ottenute
     * 
     * @param porta Porta seriale sulla quale avverrà la comunicazione
     * @param controller Controller a cui si aggencerà nella funzione risultatoTaskSeriale(String)
     */
    public void inizializzaPorta(SerialPort porta, Controller controller) {
        portaSeriale = porta;
        this.controller = controller;

        portaSeriale.setBaudRate(9600);
        portaSeriale.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        portaSeriale.addDataListener(new SerialPortDataListener() {

            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

            @Override
            public void serialEvent(SerialPortEvent event) {

                byte[] readBuffer = new byte[1024];
                portaSeriale.readBytes(readBuffer, readBuffer.length);

                String lettura = new String(readBuffer, StandardCharsets.UTF_8).trim();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.risultatoTaskSeriale(lettura);
                    }
                });

                portaSeriale.closePort(); // la porta viene chiusa dopo ogni lettura per liberarla nel periodo di inattività
                
            } 
        });

    }

    /**
     * Questa funzione ritorna la lista delle interfacce seriali connesse al computer
     * 
     * @return Un dizionario nel formato [descrizionePorta, etichettaPorta]
     */
    public Map<String, String> ottieniPorte() {
        SerialPort[] ports = SerialPort.getCommPorts();

        Map<String, String> nomi_porte = new HashMap<String, String>();
        for (SerialPort port: ports) {
            nomi_porte.put(port.getDescriptivePortName(), port.getSystemPortName());
		}

        return nomi_porte;
    }

    /**
     * Questa funzione invia un comando al dispositivo seriale connesso.
     * 
     * Oltre ai comandi personalizzati, è consigliato utilizzare quelli presenti
     * come costanti in questa classe per la comunicazione specifica con il firmware
     * di Arduino
     * 
     * @param comando Stringa che rappresenta le informazioni da inviare al dispositivo
     */
    public void scrittura(String comando) {
    	
        Task<Boolean> taskSeriale = new Task<Boolean>() { 
            @Override 
            protected Boolean call() throws Exception {

                portaSeriale.openPort();
                TimeUnit.SECONDS.sleep(2); // almeno due secondi di pausa sono necessari per assicurarsi che Arduino si riavvii completamente

                if (portaSeriale.isOpen()) {

                    byte[] byteDaScrivere =  comando.getBytes(StandardCharsets.UTF_8);
                    portaSeriale.writeBytes(byteDaScrivere, comando.length());

                } else {
                    throw new RuntimeException();
                }

                return true;
            }
 
            @Override 
            protected void succeeded() {} 
        }; 
 
        new Thread(taskSeriale).start();
        
        taskSeriale.setOnFailed(e -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.risultatoTaskSeriale(GestoreSR.CONNESSIONE_ER);
                }
            });
        });

    }
    
}
