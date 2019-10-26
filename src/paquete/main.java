/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//AÑADIDO POR ARMANDO
package paquete;

/**
 *
 * @author jose
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Thread hilo = new Thread(){
          @Override
          public void run(){
              servidor_test server = new servidor_test();
              server.startServer();
          }
        };
        hilo.start();
        
        cliente_test cliente = new cliente_test();
        cliente.startClient();
    }
    
}
