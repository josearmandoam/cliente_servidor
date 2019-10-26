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
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outWriter;
    private BufferedReader inReader;
    private int port = 29292;
    private String peticion;
    private Vector<MFile> files_available;
    private MFile mFile;
    
    //Gstion de Imagen
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
                System.out.println("ERROR AL DESCARGAR LA IMAGEN");
            else{
                System.out.println("IMAGEN DESCARGADA CORRECTAMENTE");
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

    private void proccessFile(String path){
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
    
    void readFilesAvailable(){
        File carpeta = new File("data/");
        files_available = new Vector();
        for(File fichero : carpeta.listFiles()){
            mFile = new MFile(fichero.getAbsolutePath(),fichero.getName());
            files_available.add(mFile);
        }
    }
    
}



