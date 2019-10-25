/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class servidor_test {
    private final String SERVER_LISTENING = "000";
    private final String CLIENT_OPERATIVE = "001";
    private final String OK = "010";
    private final String NOT_OK = "100";
    
    private final String IMAGES_DATA = "002";
    private final String IMAGES_SIZE = "020";
    private final String END_DATA =  "200";
    private final String DOWNLOAD_IMAGE = "222";
    
    //PETICIONES
    private final String GET_IMAGES_URLS = "0001";
    private final String END_CONECTION = "0000";
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outWriter;
    private BufferedReader inReader;
    private int port = 29292;
    private String peticion;
    private Vector<String> images_url;
    private Vector<String> v_path;
    
    //Gstion de Imagen
    FileInputStream fis;
    ObjectOutputStream oos;
    
    public servidor_test(){
        images_url = new Vector();
        images_url.add("https://i.imgur.com/49hyqOo.jpg");
        images_url.add("https://i.imgur.com/YCQQToE.jpg");
        images_url.add("https://i.imgur.com/Ccg3obV.jpg");
        
        v_path = new Vector();
        v_path.add("img/esfera.jpg");
        v_path.add("img/cubo.jpg");
        v_path.add("img/tetraedro.jpg");
        v_path.add("img/pdf-test.pdf");
        v_path.add("img/sample-audio.mp3");
        v_path.add("img/test-tgz.tgz");
        v_path.add("img/CentOS-7-x86_64-Minimal-1611.iso");
        v_path.add("img/ubuntu-16.04.5-server-amd64.iso");
    }
    
    public void iniciarServidor(){
        try {
            serverSocket = new ServerSocket(port);
            do{
                clientSocket = serverSocket.accept();
                System.out.println("\nCliente conectado: " + clientSocket.getLocalAddress() + " - " + clientSocket.getPort());
                do{
                    peticion = readMessage();
                    //inReader.close();
                    if(peticion != "-1")
                        switch (peticion) {
                            case GET_IMAGES_URLS:
                                //El cliente solicita las imagenes
                                if(!enviarImagenes())
                                    System.out.println("ERROR AL ENVIAR LAS IMAGENES");
                                else{
                                    //outWriter.close();
                                    //inReader.close();
                                    System.out.println("IMAGENES ENVIADAS CORRECTAMENTE");
                                    writeMessage(SERVER_LISTENING);
                                }
                                break;
                            case DOWNLOAD_IMAGE:
                                //El cliente solicita descargar una imagen
                                if(!images_url.isEmpty()){
                                    writeMessage(OK);
                                    if(readMessage().equals(IMAGES_DATA)){
                                        writeMessage(OK);
                                        int num_imagen = Integer.parseInt(readMessage());
                                        String path = v_path.get(num_imagen);
                                        if(path != null){
                                            writeMessage(OK);
                                            sendImage(path);
                                            if(!clientReceivedMessage())
                                                System.out.println("ERROR AL DESCARGAR LA IMAGEN");
                                            else{
                                                System.out.println("IMAGEN DESCARGADA CORRECTAMENTE");
                                                writeMessage(SERVER_LISTENING);
                                            }
                                                
                                        }
                                    }
                                }
                                break;

                            //case 333:
                                //datos de la imagen que se va a descargar
                            //    break;

                            case END_CONECTION:
                                //El cliente solicita desconectarse del servidor
                                inReader.close();
                                outWriter.close();
                                oos.close();
                                clientSocket.close();
                                clientSocket = null;
                                break;
                            default:
                                throw new AssertionError();
                        }
                    
                }while(clientSocket!=null);
            }while(true);
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean enviarImagenes(){
        boolean result = false;
        writeMessage(CLIENT_OPERATIVE);
        if(clientReceivedMessage()){
            writeMessage(IMAGES_DATA);
            if(clientReceivedMessage()){
                writeMessage(IMAGES_SIZE);
                if(clientReceivedMessage())
                    writeMessage(Integer.toString(images_url.size()));
                    for(int i=0;i<images_url.size();i++){
                        writeMessage(images_url.get(i));
                        clientReceivedMessage();
                    }
                if(clientReceivedMessage())
                    result = true;
            }
        }else{
            System.out.println("CLIENTE NO OPERATIVO");
        }
        return result;
    }
    
    private void writeMessage(String message){
        try {
            outWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            outWriter.println(message);
            //outWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String readMessage(){
        String response = "-1";
        try {
            inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            response = inReader.readLine();
            //inReader.close();
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    private boolean clientReceivedMessage(){
        return (readMessage().equals(OK));
    }

    private void sendImage(String path){
        try {
            fis = new FileInputStream(path);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(buffer);
            
            //oos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}



