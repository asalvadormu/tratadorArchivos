/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tratadorarchivos;

/**
 *
 * @author SAMUAN
 */
public class Muestra {
    
    private long tiempo;
    private double aceleracion;

    public Muestra(long tiempo, double aceleracion) {
        this.tiempo = tiempo;
        this.aceleracion = aceleracion;
    }

    public long getTiempo() {
        return tiempo;
    }

    public void setTiempo(long tiempo) {
        this.tiempo = tiempo;
    }

    public double getAceleracion() {
        return aceleracion;
    }

    public void setAceleracion(double aceleracion) {
        this.aceleracion = aceleracion;
    }
}


