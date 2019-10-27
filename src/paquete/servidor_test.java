/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paquete;

import java.io.BufferedReader;
import java.io.File;
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
    private final String DATA_SIZE = "020";
    private final String END_DATA =  "200";
    private final String DOWNLOAD_FILE = "222";
    
    //PETICIONES
    private final String GET_DATA_AVAILABLE = "0001";
    private final String END_CONNECTION = "0000";
    
    private final String SOURCE_PATH = "data/";
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outWriter;
    private BufferedReader inReader;
    private int port = 29292;
    private String peticion;
    private Vector<MFile> files_available;
    private MFile mFile;
    byte[] buffer;
    
    //Gestion de Imagen
    FileInputStream fis;
    ObjectOutputStream oos;
    
    public servidor_test(){
        readFilesAvailable();
    }
    
    public void startServer(){
        try {
            serverSocket = new ServerSocket(port);
            do{
                clientSocket = serverSocket.accept();
                inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//la linea de creacion del buffer la he puesto aqui para que no se destruya ningun mensaje

                System.out.println("\nCliente conectado: " + clientSocket.getLocalAddress() + " - " + clientSocket.getPort());
                do{
                    peticion = readMessage();
                    //inReader.close();
                    if(peticion != "-1")
                        switch (peticion) {
                            case GET_DATA_AVAILABLE:
                                //El cliente solicita los archivos del servidor
                                sendFilesAvailable();
                                break;
                            case DOWNLOAD_FILE:
                                //Se envia el fichero que se solicita
                                sendFile();
                                break;

                            //case 333:
                                //datos de la imagen que se va a descargar
                            //    break;

                            case END_CONNECTION:
                                //El cliente solicita desconectarse del servidor
                                endClientConnection();
                                break;
                            default:
                        }
                    
                }while(clientSocket != null);
            }while(true);
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void endClientConnection() {
        try{
            if(inReader!=null)
                inReader.close();
            if(outWriter!=null)
                outWriter.close();
            if(oos!=null)
                oos.close();
            if(clientSocket!=null)
                clientSocket.close();
            clientSocket = null;
        }catch(IOException e){}
    }

    private void sendFile() {
        //El cliente solicita descargar una imagen
        String id_file = readMessage();
        for(MFile file:files_available){
            if(file.getId().equals(id_file))
                mFile = file;
        }
        if(mFile != null){
            proccessFile(mFile.getPath());
            if(!clientReceivedMessage())
                System.out.println("ERROR AL ENVIAR EL ARCHIVO");
            else{
                System.out.println("ARCHIVO ENVIADO CORRECTAMENTE");
                writeMessage(SERVER_LISTENING);
            }
        }
    }
    
    private void sendFilesAvailable(){
        writeMessage(Integer.toString(files_available.size()));
        for(int i=0;i<files_available.size();i++){
            writeMessage(files_available.get(i).getName());
            clientReceivedMessage();
            writeMessage(files_available.get(i).getId());
            clientReceivedMessage();
            writeMessage(files_available.get(i).getType());
            clientReceivedMessage();
            writeMessage(files_available.get(i).getSize());
            clientReceivedMessage();
        }
        if(clientReceivedMessage())
            System.out.println("Server: Archivos enviados correctamente");
        else
            System.out.println("Server: Error al enviar los archivos disponibles");
    }
    
    private void writeMessage(String message){
        try {
            outWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            outWriter.println(message);
            
//            System.out.println("Client write: "+message);
            //outWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String readMessage(){
        String response = "-1";
        try {
            response = inReader.readLine();
            
//            System.out.println("Client reads: "+response);
            //inReader.close();
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    private boolean clientReceivedMessage(){
        return (readMessage().equals(OK));
    }

    private void proccessFile(String path){
        
            Thread read = new Thread(){
                public void run(){
                    try {
                        fis = new FileInputStream(path);
                        buffer = new byte[fis.available()];
                        fis.read(buffer);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            read.start();
            while(read.isAlive()){};
            Thread write = new Thread(){
                public void run(){
                    try {
                        oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        oos.writeObject(buffer);
                        
//                        System.out.println(oos)
                    } catch (IOException ex) {
                        Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            write.start();
            while(write.isAlive()){
                //System.out.println("Enviando archivo...");
            }
            //oos.close();
    }
    
    private void readFilesAvailable(){
        File carpeta = new File(SOURCE_PATH);
        files_available = new Vector();
        for(File fichero : carpeta.listFiles()){
            mFile = new MFile(fichero.getAbsolutePath(),fichero.getName(),getFileExtension(fichero), getFileSize(fichero));
            files_available.add(mFile);
        }
    }

    private String getFileSize(File fichero) {
        //
        String fSize = "0B";
        long size = fichero.length();
        fSize = fichero.length() + "KB";
        if(size > 1024){// pasamos B a KB
            size = (size / 1024);
            fSize =  size + "KB";
        }
        
        if(size > 1024){ //pasamos KB a MB
            size = (size/1024);
            fSize =  size + "MB";
        }
        
        if(size > 1024){ //pasamos MB a GB
            size = (size/1024);
            fSize =  size + "GB";
        }
        
        return fSize;
    }
    
    private String getFileExtension(File fichero) {
        String extesion = fichero.getName().substring(fichero.getName().indexOf(".")+1,fichero.getName().length());
        return extesion;
    }
    
}



