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
    private final String DOWNLOAD_FILE = "222";
    
    //PETICIONES
    private final String GET_FILE = "0001";
    private final String SEND_FILE_SIZE = "0002";
    private final String SEND_FILE_DATA = "0003";
    
    private final String END_CONNECTION = "0000";
    
    private final String SOURCE_PATH = "data/";
    private final String ALL_FILES = "*.*";
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outWriter;
    private BufferedReader inReader;
    private int port = 29292;
    private Vector<MFile> files_available;
    private Vector<MFile> files_available_aux;
    
    private Vector<String> v_aux;
    private Vector<String> msg_data;
    private String msg_code;
    private String msg_size;
    private MFile mFile;
    byte[] buffer;
    
    //Gestion de Imagen
    FileInputStream fis;
    ObjectOutputStream oos;
    
    public servidor_test(){
        readFilesAvailable();
        msg_data = new Vector();
        v_aux = new Vector();
    }
    
    public void startServer(){
        try {
            serverSocket = new ServerSocket(port);
            do{
                clientSocket = serverSocket.accept();
                inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//la linea de creacion del buffer la he puesto aqui para que no se destruya ningun mensaje

                System.out.println("\nCliente conectado: " + clientSocket.getLocalAddress() + " - " + clientSocket.getPort());
                do{
                    readMessage();
                        switch (msg_code) {
                            case GET_FILE:
                                if(msg_data.get(0).equals(ALL_FILES))
                                    sendAllFiles();
                                else
                                    sendFileBy(msg_data.get(0));
                                break;
                            case DOWNLOAD_FILE:
                                //Se envia el fichero que se solicita
                                sendFile();
                                break;
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

    private void buildMessage(String code, Vector<String> args, int num_items){
        //Todos los mensajes son del tipo cod#items_size#arg1#arg2...
        String msg = code+"#"+num_items;
        for (int i = 0; i < num_items; i++) {
            msg += "#"+args.get(i);
        }
        msg +="#";
        writeMessage(msg);
    }
    
    private void structMessageReceived(String msg){
        msg_code = msg.substring(0,msg.indexOf("#"));
        msg = msg.substring(msg.indexOf("#")+1,msg.length());
        msg_size = msg.substring(0,msg.indexOf("#"));
        msg = msg.substring(msg.indexOf("#")+1,msg.length());
        for (int i = 0; i < Integer.parseInt(msg_size); i++) {
            msg_data.add(msg.substring(0,msg.indexOf("#")));
            msg = msg.substring(msg.indexOf("#")+1,msg.length());
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
        //El cliente solicita descargar un archivo
        String id_file = msg_data.get(0);
        for(MFile file:files_available){
            if(file.getId().equals(id_file))
                mFile = file;
        }
        if(mFile != null){
            proccessFile(mFile.getPath());
            readMessage();
            if(msg_code.equals(DOWNLOAD_FILE) && msg_data.get(0).equals("ACK"))
                System.out.println("ARCHIVO ENVIADO CORRECTAMENTE");
            else{
                System.out.println("ERROR AL ENVIAR EL ARCHIVO");
            }
        }
    }
    
    private void sendAllFiles(){
        v_aux.clear();
        v_aux.add(Long.toString(files_available.size()));
        buildMessage(SEND_FILE_SIZE, v_aux, 1);
        for(int i=0;i<files_available.size();i++){
            v_aux.clear();
            v_aux.add(files_available.get(i).getFullName());
            v_aux.add(files_available.get(i).getId());
            v_aux.add(Long.toString(files_available.get(i).getSize()));
            buildMessage(SEND_FILE_DATA, v_aux, 3);
        }
    }
    
    private void sendFileBy(String cadena){
        readFilesAvailableByString(cadena);
        v_aux.clear();
        v_aux.add(Long.toString(files_available_aux.size()));
        buildMessage(SEND_FILE_SIZE, v_aux, 1);
        for(int i=0;i<files_available_aux.size();i++){
            v_aux.clear();
            v_aux.add(files_available_aux.get(i).getFullName());
            v_aux.add(files_available_aux.get(i).getId());
            v_aux.add(Long.toString(files_available_aux.get(i).getSize()));
            buildMessage(SEND_FILE_DATA, v_aux, 3);
        }
    }
    
    private void writeMessage(String message){
        try {
            outWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            outWriter.println(message);
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String readMessage(){
        String response = "-1";
        try {
            response = inReader.readLine();
            msg_code = "";
            msg_size = "";
            msg_data.clear();
            structMessageReceived(response);
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    private void proccessFile(String path){
        try {
            fis = new FileInputStream(path);
            buffer = new byte[fis.available()];
            fis.read(buffer);
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(buffer);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(servidor_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readFilesAvailable(){
        File carpeta = new File(SOURCE_PATH);
        files_available = new Vector();
        for(File fichero : carpeta.listFiles()){
            mFile = new MFile(fichero.getAbsolutePath(),fichero.getName(), fichero.length());
            files_available.add(mFile);
        }
    }
    
    private void readFilesAvailableByString(String cadena){
        if(files_available_aux==null)
            files_available_aux = new Vector();
        else
            files_available_aux.clear();
        
        for(MFile file : files_available){
            if(file.getFullName().toUpperCase().contains(cadena.toUpperCase())){
                files_available_aux.add(file);
            }
        }
    }
    
    public static void main(String[] args){
        servidor_test server = new servidor_test();
        server.startServer();
    }
}



