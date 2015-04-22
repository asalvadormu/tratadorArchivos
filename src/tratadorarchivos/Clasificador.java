/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tratadorarchivos;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author SAMUAN
 */
public class Clasificador {
    private Muestra[] valores;

    private long peaktime;
    private long peaktimemas;
    private int marcadorPeak=0;
    private int marcadorPeakMas;

    private int marcadorIE=0; //posición en el vector del tiempo de fin de impacto valor_IE
    private long valor_IE =0;

    private int marcadorIS=0; //posición en el vector del tiempo de inicio de impacto valor_IS
    private long valor_IS =0;

    private double valor_AAMV;
    private double valor_IDI;
    private double valor_MPI;
    private double valor_MVI;
    private double valor_PDI;
    private double valor_ARI;
    private double valor_FFI;
    private double valor_SCI;
    
    private double[] caracteristicas;
    
      /**
     * Calcula los 8 valores necesarios para el clasificador
     *
     * @param peaktime tiempo de pico seguido de 2500 ms sin picos.
     * @param valores array con todos los valores capturados de aceleración
     */
    Clasificador(long peaktime,Muestra[] valores){
        System.out.println("CLASIFICADOR ENTRANDO EN CLASE");

        this.peaktime=peaktime;
        this.valores=valores;       
        
        //buscar indice de tiempo de pico
        for(int i=0;i<valores.length;i++) {
            long eltiempo = valores[i].getTiempo();
            if (eltiempo >= peaktime) {
                marcadorPeak = i;
                break;
            }
        }
        
        //buscar indice de tiempo de pico mas 1 segundo
        peaktimemas=peaktime+1000000000; //nanosegundos      
        for(int i=marcadorPeak;i<valores.length;i++){
            if(valores[i].getTiempo()>peaktimemas){
                marcadorPeakMas=i;
                break;
            }
        }
        
        System.out.println("TIEMPO PICO "+peaktime+" MARCA "+marcadorPeak);
        System.out.println("TIEMPO PICO MAS "+peaktimemas+" MARCA "+marcadorPeakMas);

        
        
        System.out.println("CLASIFICADOR Calculo valores iniciales");
        //cálculo valores iniciales.
        calcularIE();
        calcularIS();

        System.out.println("CLASIFICADOR Calculo Caracteristicas");
        //a partir de aqui son los cálculos de los ocho valores.
        valor_AAMV=calcularAAMV();
        valor_IDI=calcularIDI();
        valor_MPI=calcularMPI();
        valor_MVI=calcularMVI();
        valor_PDI=calcularPDI();
        valor_ARI=calcularARI();
        valor_FFI=calcularFFI();
        valor_SCI=calcularSCI();
       
        caracteristicas=new double[8];
        caracteristicas[0]= valor_AAMV;
        caracteristicas[1]= valor_IDI;
        caracteristicas[2]= valor_MPI;
        caracteristicas[3]= valor_MVI;
        caracteristicas[4]= valor_PDI;
        caracteristicas[5]= valor_ARI;
        caracteristicas[6]= valor_FFI;
        caracteristicas[7]= valor_SCI;
    }

    public double[] getCaracteristicas() {
        return caracteristicas;
    }
    
    

    /**
     * Calcula el tiempo de fin de impacto. Última aceleracion mayor a 1.5 g dentro del intervalo [peaktime, peaktime + 1000]
     * @return
     */
    private void calcularIE(){
        for(int j=marcadorPeakMas; j>=marcadorPeak; j--){
            if(valores[j].getAceleracion()>1.5) {
                valor_IE =valores[j].getTiempo();
                marcadorIE=j;
                break;
            }
        }
        System.out.println("valor_IE: "+valor_IE);
    }

    /**
     * Calcula el tiempo de Inicio del impacto. Tiempo de primera aceleracion > 1.5 precedida de aceleración < 0.8 en
     * intervalo [ valor_IE-1200, Peaktime ]
     * @return
     */
    private void calcularIS() {
        long tiempoIEmenos = valor_IE - 1200000000;
        int marcadorTiempoIEMenos = 0;
        for (int i = 0; i < valores.length; i++) {
            long eltiempo = valores[i].getTiempo();
            if (eltiempo >= tiempoIEmenos) {
                marcadorTiempoIEMenos = i-1;
                break;
            }
        }
        //buscar un valor <0.8 y despues un valor > 1.5

        //si no aparece < 0.8 o despues >1.5 entonces peaktime.
        boolean elmenor=false;
        boolean elmayor=false;
        for(int j=marcadorTiempoIEMenos;j==marcadorPeak;j++){

            if(!elmenor){
                if(valores[j].getAceleracion()<0.8){
                    elmenor=true;
                }
            }
            if(elmenor && !elmayor){
                if(valores[j].getAceleracion()>1.5){
                    elmayor=true;
                    marcadorIS=j;
                    valor_IS =valores[j].getTiempo();
                }
            }
        }
        if(!elmenor || !elmayor) {
            marcadorIS=marcadorPeak;
            valor_IS =peaktime;
        }
        System.out.println("valor_IS "+valor_IS);
    }

    /**
     * Calcula el promedio absoluto de aceleración
     * En el intervalo [valor_IS,valor_IE]
     *
     */
    private double calcularAAMV(){
        double difTotal=0;
        if(marcadorIS>0 && marcadorIE<=valores.length) {
            for(int i=marcadorIS;i<=marcadorIE;i++){
                double dif=Math.abs( valores[i].getAceleracion() - valores[i+1].getAceleracion() );
                difTotal=difTotal+dif;
            }
            difTotal=difTotal/(marcadorIE-marcadorIS+1);
            System.out.println("Acelerometro difTotal: " + difTotal);
        }
        System.out.println("Acelerometro Marcadores "+marcadorIS+" "+marcadorIE+" AAMV "+difTotal);
        return difTotal;
    }

    /**
     * Calcula el indice de duración de impacto. IDI
     * Diferencia entre el tiempo valor_IE y el tiempo valor_IS
     */
    private double calcularIDI(){
        long tiempoFinal=valores[marcadorIE].getTiempo();
        long tiempoInicio=valores[marcadorIS].getTiempo();
        long tiempoAdevolver=(tiempoFinal-tiempoInicio)/1000000;
        return tiempoAdevolver; //nanosegundos.
    }

    /**
     * Calcula el valor del indice de valor máximo de aceleración. MPI
     * En el intervalo [valor_IS,valor_IE]
     * @return el valor de MPI en g.
     */
    private double calcularMPI(){
        double maxAceleracion =0;
        if(marcadorIS>0 && marcadorIE<=valores.length) {
            for(int i=marcadorIS;i<=marcadorIE;i++){
                if(maxAceleracion< valores[i].getAceleracion() ) {
                    maxAceleracion = valores[i].getAceleracion();
                }
            }
        }
        System.out.println("Acelerometro MPI: "+maxAceleracion);
        return maxAceleracion;
    }

    /**
     * Calcula el índice de valle mínimo. MVI.
     * Es el valor mínimo de aceleración en el intervalo [valor_IS-500,valor_IE]
     * Útil para distinguir choques de caidas.
     *
     * @return el valor mínimo de aceleración.
     */
    private double calcularMVI(){
        //calcular posición valor_IS-500 000 000
        long tiempoISmenos = valor_IS - 500000000;
        int marcadorTiempoISMenos = 0;
        for (int i = 0; i < valores.length; i++) {
            long eltiempo = valores[i].getTiempo();
            if (eltiempo >= tiempoISmenos) { //en el peor caso marcará el valor i=0;
                marcadorTiempoISMenos = i;
                break;
            }
        }
        //calcular el MVI.
        double minAceleracion =1; //pongo 1 g. Una caida debe bajar de 1g.
        if(marcadorTiempoISMenos>0 && marcadorIE<=valores.length) {
            for(int i=marcadorTiempoISMenos;i<=marcadorIE;i++){
                if(minAceleracion> valores[i].getAceleracion() ) {
                    minAceleracion = valores[i].getAceleracion();
                }
            }
        }
        System.out.println("Acelerometro MVI: "+minAceleracion);
        return minAceleracion;
    }

    /**
     * Calcula el índice de duración de pico. PDI.
     * Diferencia entre el PS comienzo de pico y PE fin de pico.
     * PS es el tiempo último muestreo <1.8g antes del pico
     * PE es el tiempo del primer muestreo <1.8g despues del pico.
     *
     * @return el valor de PDI en segundos.
     */
    private double calcularPDI(){
        int marcaPS=0;
        int marcaPE=valores.length-1;
        for(int i=marcadorPeak-1;i>=0;i--){
           if( valores[i].getAceleracion() <1.8){
               marcaPS=i;
               break;
           }
        }
        for(int i=marcadorPeak+1;i<valores.length;i++){
            if(valores[i].getAceleracion()<1.8){
                marcaPE=i;
                break;
            }
        }
        double valor=valores[marcaPE].getTiempo()-valores[marcaPS].getTiempo();
        valor=valor/1000000; //pasar a milisegundos.
        return  valor;
    }

    /**
     * Calcula el indice de ratio de actividad. ARI.
     * Mide el nivel de actividad en 700ms centrados en [valor_IS,valor_IE]
     * Ratio entre número de muestras no en [0.8g,1.3g] y total muestras en el intervalo.
     * Cuanto más alto mayor actividad.
     *
     * @return El valor de ARI.
     */
    private double calcularARI(){
        int marcaInicio=marcadorIS;
        int marcaFin=marcadorIE;
        double dif= valor_IE - valor_IS;
        //calculo intervalo centrado
        if( dif > 700000000 ){
            double tiempoAnte=dif/2-350000000+ valor_IS;
            double tiempoDes=dif/2+350000000+ valor_IS;

            //buscar las posiciones de esos tiempos.
            for(int i=0;i<valores.length;i++) {
               if( valores[i].getTiempo()>tiempoAnte){
                   marcaInicio=i-1;
                   if(marcaInicio<0) marcaInicio=0;
                   break;
               }
            }
            for(int i=0;i<valores.length;i++) {
                if(valores[i].getTiempo()>tiempoDes){
                    marcaFin=i;
                    break;
                }
            }
        }
        //calculo indice
        double totalmuestras=marcaFin-marcaInicio+1;
        double muestrasEnIntervalo=0;
        for(int i=marcaInicio;i<=marcaFin;i++){
            double ace=valores[i].getAceleracion();
            if( ace<0.8 || ace>1.5 ){
                muestrasEnIntervalo++;
            }
        }
        double resultado=muestrasEnIntervalo/totalmuestras;
        return resultado;
    }


    /**
     * Calcula el indice de caida libre. FFI.
     * Busca muestra <0.8g 200ms antes del pico. El tiempo encontrado es el fin del intervalo, si no 200ms.
     * Inicio del intervalo 200ms antes del fin.
     *
     * Se calcula como media de valores de aceleración
     * saltos 0.1g caidas minimo 0.6, media 1.1g
     *
     * @return el valor de FFI
     */
    private double calcularFFI(){
        int marcaInicio=0;
        int marcaFin=marcadorPeak;

        //averiguar fin de intervalo
        for(int i=marcadorPeak-1;i>=0;i--){
           double difTiempo=peaktime-valores[i].getTiempo();
           if(valores[i].getAceleracion() < 0.8   ){
               marcaFin=i;
               break;
           }else if( difTiempo>200000000  ){
               if(i==marcadorPeak-1){
                   marcaFin=i;
               }else{
                   marcaFin=i-1;
               }
               break;
           }
        }
        //averiguar inicio intervalo.
        double tiempoInicio=valores[marcaFin].getTiempo()-200000000;
        for(int i=marcaFin-1;i>=0;i--){
            double difTiempo=tiempoInicio-valores[i].getTiempo();
            if(difTiempo>200000000){
                marcaInicio=i;
                break;
            }
        }
        //calcular media.
        double contador=0;
        for(int i=marcaInicio;i<=marcaFin;i++){
            contador=contador+valores[i].getAceleracion();
        }
        return contador/(marcaFin-marcaInicio+1);
     }


    /**
     * Calcula el indice de contador de pasos. SCI
     * Contar valles 2200ms antes del PT.
     * Valle aceleración < 1g durante al menos 80 ms
     * seguido de pico >1.6g dentro de 200 ms.
     * Valles consecutivos separados más de 200ms.
     *
     * @return el valor de SCI
     */
    private double calcularSCI(){
        //calcular marca en tiempo anterior a 2200ms
        int marcaInicio=0;
        double tiempoInicio=peaktime-2200000000d;
        for(int i=marcadorPeak-1;i<=0;i--){
            if( valores[i].getTiempo() <tiempoInicio){
                marcaInicio=i;
                break;
            }
        }

        String modo="inicio";
        double tiempoInicioValle=0;
        double tiempoFinValle=0;
        double tiempoPasoAnterior=0;
        int marcaFinValle=0;
        int contadorPasos=0;

        //calculo pasos
        int marcaActual=0; //indica el indice actual que se esta comprobando.
        while(marcaActual<marcadorPeak){
            if( modo.equals("inicio")){

                if(valores[marcaActual].getAceleracion()<1){
                    tiempoInicioValle=valores[marcaActual].getTiempo();
                    modo="valle";
                }

                marcaActual++;

            } else if( modo.equals("valle")){
                if(valores[marcaActual].getAceleracion()>=1){
                    tiempoFinValle=valores[marcaActual-1].getTiempo();
                    double dif=tiempoFinValle-tiempoInicioValle;
                    if(dif>80000000){
                        marcaFinValle=marcaActual-1;
                        if(contadorPasos>0     ){
                            if( (tiempoFinValle-tiempoPasoAnterior)>200000000 ){
                                modo="pico";
                            }else{
                                modo="inicio";
                            }
                        }else{
                            modo="pico";
                        }

                    }else{
                        modo="inicio";
                    }
                }
                marcaActual++;

            }else if(modo.equals("pico")){
                double difLimite= valores[marcaActual].getTiempo()- tiempoFinValle;
                if(difLimite<200000000){
                    if(valores[marcaActual].getAceleracion()>1.6) {
                        contadorPasos++;
                        tiempoPasoAnterior=valores[marcaFinValle].getTiempo();
                        modo="inicio";
                    } else{
                        //no hago nada
                    }
                }else{
                    modo="inicio";
                    marcaActual=marcaFinValle;
                }
                marcaActual++;
            }
        }
        return contadorPasos;
    }

}
