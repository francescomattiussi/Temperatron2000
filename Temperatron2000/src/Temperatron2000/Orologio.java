package Temperatron2000;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;

/**
 * Questa classe si occupa di gestire il timer e tutte le funzioni a esso associate
 */
public class Orologio {

    private Timer timer;
    private Controller controller;
    private long secondi;

    /**
     * Metodo di inizializzazione della classe Orologio. La classe principale da cui verrà 
     * inizializzata questa classe dovrà essere di tipo Controller e dovrà implementare le 
     * funzioni aggironaTempoRimanente(long) e tempoEsaurito()
     * 
     * @param secondi Durata del timer espressa in secondi
     * @param controller Controller al quale agganciarsi per le funzioni di callback
     */
    public Orologio(long secondi, Controller controller) {
        timer = new Timer();

        this.controller = controller;
        this.secondi = (int)secondi;
    }

    /**
     * Avvia il countdown del timer
     */
    public void avvia() {
      timer.schedule(new avviaTimer(), 0, 1000);
    }

    /**
     * Arresta il countdown del timer e chiama la funzione tempoEsaurito() sulla classe
     * principale
     */
    public void arresta() {
      timer.cancel();
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
            controller.tempoEsaurito();
        }
      });
    }

    /**
     * Sottoclasse che implementa le funzioni della superclasse TimerTask per gestire 
     * l'esecuzione asincrona del timer
     */
    private class avviaTimer extends TimerTask {
        
        @Override
        public void run() {
          if (secondi > 0) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.aggiornaTempoRimanente(secondi);
                }
            });
            
            secondi--;

          } else {
            timer.cancel();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    controller.tempoEsaurito();
                }
            });
          }
        }
      }
}