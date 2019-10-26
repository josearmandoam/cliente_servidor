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
    private final String DATA_SIZE = "020";
    private final String END_DATA =  "200";
    private final String DOWNLOAD_FILE = "222";
    
    //PETICIONES
    private final String GET_DATA_AVAILABLE = "0001";
    private final String END_CONNECTION = "0000";
    
    private final String SAVE_FILES_PATH = "img/";
    
    
    private Socket clientSocket;
    private int port = 29292;
    private String host = "localhost";
    private BufferedReader inReader;
    private PrintWriter outWriter;
    private Vector<MFile> files_available;
    private String response;
    private MFile mFile;
    //Gestion Imagen
    ObjectInputStream ois;
    FileOutputStream fos;
    
    public cliente_test(){
        files_available = new Vector();
    }
    
    public void startClient(){
        try {
            clientSocket = new Socket(host, port);
            getFilesAvailable();
            getFile(1);
            endConnection();
        } catch (IOException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getFile(int objectSelected) {
        //SOLICITAR DESCARGAR UN IMAGEN
        writeMessage(DOWNLOAD_FILE);
        writeMessage(files_available.get(objectSelected).getId());
        takeFile(files_available.get(objectSelected).getName());
        writeMessage(OK);
        writeMessage(END_CONNECTION);
    }

    private void getFilesAvailable(){
        writeMessage(GET_DATA_AVAILABLE);
        int data_size = Integer.parseInt(readMessage());
        String name,id;
        for(int i=0;i<data_size;i++){
            name = readMessage();
            writeMessage(OK);
            id = readMessage();
            mFile = new MFile("unknow",name, id);
            files_available.add(mFile);
            writeMessage(OK);
        }
        
        if(files_available.size() == data_size)
            writeMessage(OK);
        
        System.out.println("Cliente: \nArchivos disponibles:");
        for(int i=0;i<files_available.size();i++){
            System.out.println(files_available.get(i));
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
    
    private void takeFile(String name){
        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
            byte[] buffer = (byte[]) ois.readObject();
            
            fos = new FileOutputStream(SAVE_FILES_PATH + name);
            fos.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void endConnection() {
        writeMessage(END_CONNECTION);
        try{
            if(inReader!=null)
                inReader.close();
            if(outWriter!=null)
                outWriter.close();
            if(ois!=null)
                ois.close();
            if(clientSocket!=null)
                clientSocket.close();
        }catch(IOException e){}
        clientSocket = null;
    }
}
