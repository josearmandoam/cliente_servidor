/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//Pepe ha escrito aqui
package client.server;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class cliente_test {
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
    
    
    private Socket clientSocket;
    private int port = 29292;
    private String host = "localhost";
    private BufferedReader inReader;
    private PrintWriter outWriter;
    private Vector<String> images_url;
    private String response;
    
    //Gestion Imagen
    ObjectInputStream ois;
    FileOutputStream fos;
    
    public cliente_test(){
        images_url = new Vector();
    }
    
    public void iniciarCliente(){
        try {
            clientSocket = new Socket(host, port);
            writeMessage(GET_IMAGES_URLS);
            //while(readMessage() != CLIENT_OPERATIVE){}
            clientOperative();
            
            if(readMessage().equals(IMAGES_DATA)){
                messageReceived();
                if(readMessage().equals(IMAGES_SIZE)){
                    messageReceived();
                    int size = Integer.parseInt(readMessage());
                    for(int i=0;i<size;i++){
                        response  = readMessage();
                        messageReceived();
                        images_url.add(response);
                    }
                }
                    
            }
            messageReceived();
            System.out.println("INFO RECIBIDA:");
            for(int i=0;i<images_url.size();i++){
                System.out.println(images_url.get(i));
            }
            
            //SOLICITAR DESCARGAR UN IMAGEN
            if(isServerListening()){
                writeMessage(DOWNLOAD_IMAGE);
                if(serverAgree()){
                    writeMessage(IMAGES_DATA);
                    serverAgree();
                    writeMessage("7");
                    serverAgree();
                    takeImage();
                    messageReceived();
                }
            }
            
            if(isServerListening()){
                writeMessage(END_CONECTION);
                outWriter.close();
                inReader.close();
                ois.close();
                fos.close();
                clientSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeMessage(String message){
        try {
            outWriter = new PrintWriter(clientSocket.getOutputStream(),true);
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
    
    private void messageReceived(){
        writeMessage(OK);
    }
    
    private boolean serverAgree(){
        return (readMessage().equals(OK));
    }
    
    private void clientOperative(){
        if(readMessage().equals(CLIENT_OPERATIVE))
            writeMessage(OK);
    }
    
    private void takeImage(){
        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
            byte[] buffer = (byte[]) ois.readObject();
            
            fos = new FileOutputStream("img/ubuntu-16.04.5-server-amd64-download.iso");
            fos.write(buffer);
            //fos.close();
        } catch (IOException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean isServerListening(){
        return (readMessage().equals(SERVER_LISTENING));
    }
}
