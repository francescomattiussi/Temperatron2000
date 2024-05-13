package Temperatron2000;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;

/**
 * Questa classe si occupa di gestire la parte relativa allo scambio di informazioni con il database SQLite
 */

public class GestoreDB {

    private String database_url; 
    private String database_table;
    private Controller controller;
    
    public static String[] DEFAULT_HEADERS = {"ID", "temperatura", "data", "ora"};

    /**
     * Metodo di inizializzazione della classe. Nella configurazione predefinita il database
     * si trova in 'jdbc:sqlite:database/lett_temp.db' e prende come riferimento la tabella 'temperature'.
     * Si richiede il Controller che lo chiama per notificare lo stato di esportazione del file CSV
     * chiamando la funzione mostraNotifica(AlertType, String) presente all'interno di esso.
     * 
     * @param database_url Posizione in cui è salvato il file del database
     * @param database_table Tabella di riferimento
     * @param controller Controller a cui si aggancerà per notificare lo stato di esportazione del file CSV
     */
    public GestoreDB(String database_url, String database_table, Controller controller) {
        this.database_url = database_url;
        this.database_table = database_table;
        this.controller = controller;
    }
    
    /**
     * Questa funzione permette la scrittura di un oggetto di tipo Record all'interno del database
     * 
     * @param record Oggetto di tipo Record contenente le informazioni da inserire nella tabella
     */
    public void scritturaRecord(Record record) {
    	
    	try (Connection conn = DriverManager.getConnection(database_url); Statement stmt = conn.createStatement()) {

    		stmt.executeUpdate("INSERT INTO " + database_table + " VALUES(NULL, '" + record.getTemperatura() + "', '" + record.getData() + "', '" + record.getOra() + "')");
                

        } catch (SQLException e) {
            throw new Error("Problema nella scrittura, ", e);
        }
    }

    /**
     * Questa funzione permette di leggere tutti gli oggetti di tipo Record presenti nel database
     * 
     * @return Un array contenente gli oggetti di tipo Record presenti nel database
     */
    public ArrayList<Record> letturaRecords() {
        
        try (Connection conn = DriverManager.getConnection(database_url); Statement stmt = conn.createStatement()) {

	        ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + database_table);
	        ArrayList<Record> records = new ArrayList<Record>();
	
	        while (resultSet.next()) {
	            Record record = new Record(resultSet.getInt("id"), resultSet.getFloat("temperatura"), resultSet.getString("data"), resultSet.getString("ora"));
	            records.add(record);
	        }
	
	        return records;

        } catch (SQLException e) {
            throw new Error("Problema nella lettura, ", e); // CSVWriter writer = new CSVWriterBuilder(new FileWriter(file)).withSeparator('\t').build();
        }
    }
    
    /**
     * Questa funzione è la seconda parte che compone la sequenza di esportazion in formato
     * CSV dei Record presenti nel database e viene chiamata da esportaCSV() in Controller.
     * 
     * In questa parte viene costruito il file CSV, scritto nella directory specificata e viene
     * riportata una notifica che mostra l'esito dell'operazione, sia positivo che negativo.
     * 
     * @param file Oggetto File che rappresenta la directory in cui scrivere il file
     * @param headers Array String[] con i titoli per le colonne, default GestoreDB.DEFAULT_HEADERS
     */
    public void scritturaRecordsCSV(File file, String[] headers) {
    	
    	Task<String> task_csv = new Task<String>() { 
            @Override 
            protected String call() throws Exception { 
	        	try (Connection conn = DriverManager.getConnection(database_url); Statement stmt = conn.createStatement()) {
	
                    ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + database_table);
                    
                    ICSVWriter csvWriter = new CSVWriterBuilder(new FileWriter(file)).withSeparator('\t').build();
                    
                    csvWriter.writeNext(headers); // scrive gli headers

                    while (resultSet.next()) {
                    	String[] stringRecord = {resultSet.getString("id"), resultSet.getString("temperatura"), resultSet.getString("data"), resultSet.getString("ora")};
                        csvWriter.writeNext(stringRecord);
                    }
                    
                    csvWriter.close();
                    
                    return file.getAbsolutePath();
	
	            } catch (Exception e) {
	                throw new Error("Problema nella scrittura del file CSV, ", e);
	            }
	        	
            } 
 
            @Override 
            protected void succeeded() { 
            	Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        controller.mostraNotifica(AlertType.INFORMATION, "Il file CSV è stato esportato con successo!", "Esportazione CSV");
                    }
                });
            }
            
            @Override 
            protected void failed() { 
            	Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    	controller.mostraNotifica(AlertType.ERROR, "C'è stato un problema nell'esportazione del fil CSV", "Esportazione CSV");
                    }
                });
            }
            
        }; 
 
        new Thread(task_csv).start();
    	
    }
}
