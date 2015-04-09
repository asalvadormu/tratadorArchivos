/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tratadorarchivos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;

/**
 *
 * @author SAMUAN
 */
public class TratadorArchivos {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        Boolean capturar = false;
        double gravedad=9.8066;
        LinkedList<Muestra> listaCompleta=new LinkedList<Muestra>();
        int tiposalida=0;// 1 2 3 4 sentarse salto golpe caida
        
        
        //capturar argumentos
        //directorio de entrada
        //tipo de salida
        for (String s: args) {
            System.out.println(s);
        }
        String directorio=args[0];
        tiposalida=Integer.parseInt(args[1]);
    
        File f=new File(directorio);
        if (f.exists()){  
            File[] ficheros = f.listFiles();
            for (int x=0;x<ficheros.length;x++){
                System.out.println();
                System.out.println(ficheros[x].getName());
                System.out.println();
                
                listaCompleta.clear();
                
                try {
            // Apertura del fichero y creacion de BufferedReader para poder
            // hacer una lectura comoda (disponer del metodo readLine()).
                    String nombrearchivo=directorio+"/"+ficheros[x].getName();
                    archivo = new File (nombrearchivo);
                    fr = new FileReader (archivo);
                    br = new BufferedReader(fr);
            
                    // Lectura del fichero
                    String resultado="";
                    String linea;
                    while((linea=br.readLine())!=null){
                    //  System.out.println(linea);
                
                        if(capturar){
                            String[] valores = linea.split(",");
                            double[] valoresD=new double[4];
                            double acele=0;
                            for(int i=0;i<valores.length;i++){
                                valoresD[i]=Double.parseDouble(valores[i]);  
                                if(i>0){
                                    valoresD[i]=valoresD[i]/gravedad;
                            // System.out.println(valoresD[i]);
                                    acele=acele+Math.pow(valoresD[i],2);
                                }
                            }
                            acele= Math.sqrt(acele);          
                            
                    //  System.out.println(valoresD[0]+" "+acele);
                   // resultado=resultado+valoresD[0]+","+acele+"\r\n";
                            listaCompleta.add( new Muestra((long)valoresD[0],acele));     
                        }
                
                        if( linea.compareTo("@DATA")==0){
                            capturar=true;
                        }                
                    }
                    
                    capturar=false;
                     //terminado el proceso de archivo de entrada
                     //Iniciar monitor para comprobar si es caida u otra cosa.
                    Monitor monitor=new Monitor();
                    monitor.tratar(listaCompleta);            
                    double[] resul= monitor.getResultadoCara();   
                    if(resul!=null){
                        for(double valor:resul){
                            resultado=resultado+""+valor+", ";
                        }
                        switch(tiposalida){
                            case 1: 
                                resultado += "1, 0, 0, 0"; //sentarse , tumbarse
                            break;
                            case 2: 
                                resultado += "0, 1, 0, 0"; //saltar, correr, andar
                            break;
                            case 3: 
                                resultado += "0, 0, 1, 0"; //golpe en el sensor
                            break;
                            case 4: 
                                resultado += "0, 0, 0, 1"; //caida
                            break;
                        }
                    
                        //escritura en fichero nuevo
                        PrintWriter writer = new PrintWriter(
                                new BufferedWriter(
                                        new FileWriter("C://dev/src/salida.txt",true)));
                        writer.println(resultado);
                        //writer.write(resultado);
                        writer.close();   
                    }   
                    
                    
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                // En el finally cerramos el fichero, para asegurarnos
                // que se cierra tanto si todo va bien como si salta 
                // una excepcion.
                    try{                    
                        if( null != fr ){   
                            fr.close();     
                        }                  
                    }catch (Exception e2){ 
                        e2.printStackTrace();
                    }
                }                               
            }
        }else { 


        }
 
    }
    
     
}
