/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tratadorarchivos;

import java.util.LinkedList;

/**
 *
 * @author SAMUAN
 */
public class Monitor {
    
    private LinkedList<Muestra> lista; //
    private int tamaLista=500;
    private String estado="muestreo";
    private double umbralGravedad=2.5;
    private long pt=0; //peak time
    private long contadorTiempo=0;
    Muestra[] datos=null;
    private double[] resultadoCara;
    
    Monitor(){
        System.out.println("Comienzo monitor");
        iniciarLista();
        //resultadoCara=new double[8];
    }
    
    public void tratar(LinkedList<Muestra> flujoEntrada){
        
       // imprimirLista(flujoEntrada);
        
        //para cada dato hacer proceso 
        System.out.println("Monitor.tratar "+flujoEntrada.size());
        for(Muestra muestra:flujoEntrada){
            cargarMuestra(muestra);
            double modulo=muestra.getAceleracion();
            long tiempo=muestra.getTiempo();
        
            if(estado.equals("muestreo")){
                if(muestra.getAceleracion()>umbralGravedad) iniciarPostpeak(muestra.getTiempo());
            }
            
            if(estado.equals("postpeak")){
                contadorTiempo=tiempo-pt;
                if(modulo>umbralGravedad) iniciarPostpeak(tiempo);
                if(contadorTiempo>2500000000l){
                    //generar array de valores.           
                    datos=new Muestra[ lista.size()];
                    lista.toArray(datos);
                    
                    //imprimirArray();
                    
                    //System.out.println("iniciar activity test "+tiempo);
                    iniciarActivityTest();                   
                }
            }  
        }                
    }
    
    private void iniciarPostpeak(long tiempo){
        contadorTiempo=0;
        pt=tiempo;
        estado="postpeak";
       // System.out.println("iniciar post peak "+tiempo);
    }
        
    private void iniciarActivityTest(){
        //capturar datos de lista
        estado="activitytest";
       // Log.i("Acelerometro","iniciar activity test");

        //grabar a archivo externo para mostrar gráfico.
       // grabar();

        //calcular AAMV , media de las diferencias.
        long tiempoInicioCalculo =pt + 1000000000; //se toma desde 1 sg a 2.5 sg despues del impacto
        int marcador=0;
        double difTotal=0;
        long tiempoFinalCalculo = pt + 2500000000l;
        int marcadorFin=datos.length;
        
        
        System.out.println("tiempo pt, tiempo inicio, tiempo final "+pt+" "+tiempoInicioCalculo+" "+tiempoFinalCalculo);
        
       // imprimirArray();

        for(int i=0;i<datos.length;i++){
            //buscar el dato con tiempo > tiempoInciocalculo
           //System.out.println("dato i "+datos[i].getTiempo()  );
           if( datos[i].getTiempo()>tiempoInicioCalculo ){
               marcador=i;
               break;
           }
        }
        for(int i=marcador;i<datos.length;i++){
            if(datos[i].getTiempo()>tiempoFinalCalculo){
                marcadorFin=i;
                break;
            }
        }
        System.out.println(" marcadores Ini Fin "+marcador+" "+marcadorFin);
        
        for(int j=marcador;j<datos.length-2;j++){
            double dif=Math.abs( datos[j].getAceleracion() - datos[j+1].getAceleracion() );
            difTotal=difTotal+dif;
        }
        System.out.println("dif antes division "+difTotal);
        difTotal=difTotal/(marcadorFin-marcador);
        //Log.i("Acelerometro","difTotal: "+difTotal);

        //si valor supera 0.05g entonces se descarta como caida
        //si es menor o igual se considera caida y se envian datos a clasificador
        if(difTotal>0.05){
            System.out.println("caida descartada");
        }else {
            System.out.println("CAIDA");
            Clasificador clasificador = new Clasificador(pt, datos);
            resultadoCara=clasificador.getCaracteristicas();
        }
        estado="muestreo";
        
    }

    public double[] getResultadoCara() {
        return resultadoCara;
    }
    
    
    
    private void cargarMuestra(Muestra muestra){
        lista.add(muestra);
        if(lista.size()>tamaLista) lista.poll();
    }
    
    private void iniciarLista(){
        lista=new LinkedList<>();
        for(int i=0;i<tamaLista;i++){
            lista.add(new Muestra(0,1));
        }
    }
    
     private void imprimirMuestra(){
        for(int i=0;i<lista.size();i++){
            Muestra mu= lista.get(i);
            System.out.println( "Muestra "+i+" tiempo: +"+mu.getTiempo()+" acele: "+mu.getAceleracion());
        }

        long dif=lista.get(lista.size()-1).getTiempo()- lista.get(0).getTiempo();
        System.out.println("Muestra diferencia tiempo: "+dif );
    }
     
    private void imprimirArray(){
        if(datos!=null){
            for(int i=0;i<datos.length;i++){
                Muestra mu=datos[i];
                System.out.println("Muestra Dato: "+i+" tiempo: +"+mu.getTiempo()+" acele: "+mu.getAceleracion());
            }
        }else{
            System.out.println("Muestra datos nulos ");
        }
    }
    
    private void imprimirLista(LinkedList<Muestra> dat){
        for(int i=0;i<dat.size();i++){
             Muestra mu= dat.get(i);
             System.out.println( "Muestra "+i+" tiempo: +"+mu.getTiempo()+" acele: "+mu.getAceleracion());
        }
    }
    
}
