package Temperatron2000;

/**
 * Questa classe rappresenta l'oggetto Record. Si è scelto di usare una struttura dati personalizzata
 * rispetto alla classe standard java.lang.Record per avere maggior controllo e chiarezza sulla
 * composizione dell'elemento.
 */
public class Record {
    private int ID;
    private float temperatura;
    private String data;
    private String ora;

    /**
     * Per una maggiore versatilità l'ogetto si può dichiarare anche vuoto e in seguito popolarlo
     */
    public Record() {}

    /**
     * Inizializzazione e popolazione dell'oggetto Record
     * 
     * @param ID ID del Record espresso come numero intero, nella scrittura non viene preso in considerazione 
     * 		e viene utilizzato un valore NULL per permettere l'incremento automatico da parte del database
     * @param temperatura Valore della temperatura espresso in Float
     * @param data Stringa rappresentante la data nel formato "dd/MM/yyyy"
     * @param ora Stringa rappresentante l'ora nel formato "HH:mm"
     */
    public Record(int ID, float temperatura, String data, String ora) {
        this.ID = ID;
        this.temperatura = temperatura;
        this.data = data;
        this.ora = ora;
    }

    /**
     * Imposta l'ID dell'oggetto Record
     * @param ID ID dell'oggetto espresso come numero intero
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Imposta la temperatura dell'oggetto Record
     * @param temperatura Valore della temperatura espresso in Float
     */
    public void setTemperatura(float temperatura) {
        this.temperatura = temperatura;
    }

    /**
     * Imposta la data dell'oggetto Record
     * @param data Stringa rappresentante la data nel formato "dd/MM/yyyy"
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Imposta l'ora dell'oggetto Record
     * @param ora Stringa rappresentante l'ora nel formato "HH:mm"
     */
    public void setOra(String ora) {
        this.ora = ora;
    }

    /**
     * Ritorna l'ID dell'oggetto Record
     * @return L'ID dell'oggetto espresso come numero intero
     */
    public int getID() {
        return ID;
    }

    /**
     * Ritorna la temperatura dell'oggetto Record
     * @return La temperatura dell'oggetto espressa in Float
     */
    public float getTemperatura() {
        return temperatura;
    }

    /**
     * Ritorna la data dell'oggetto Record
     * @return La data dell'oggetto espressa come Stringa nel formato "dd/MM/yyyy"
     */
    public String getData() {
        return data;
    }

    /**
     * Ritorna l'ora dell'oggetto Record
     * @return L'ora dell'oggetto espressa come Stringa nel formato "HH:mm"
     */
    public String getOra() {
        return ora;
    }
}
