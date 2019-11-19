/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paquete;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
    private final String DOWNLOAD_FILE = "222";
    
    //PETICIONES
    private final String GET_FILE = "0001";
    private final String SEND_FILE_SIZE = "0002";
    private final String SEND_FILE_DATA = "0003";
        
    private final String END_CONNECTION = "0000";
    
    private Socket clientSocket;
    private int port = 29292;
    private String host = "localhost";
    private BufferedReader inReader;
    private PrintWriter outWriter;
    protected Vector<MFile> files_available;
    private MFile mFile;
    private byte[] buffer; 
    private Vector<String> v_aux;
    private Vector<String> msg_data;
    private String msg_code;
    private String msg_size;
    //Gestion Imagen
    ObjectInputStream ois;
    FileOutputStream fos;
    
    public cliente_test(){
        
    }
    
    public boolean startClient(String host, int port){
        try {
            clientSocket = new Socket(host, port);
            inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //la linea de creacion del buffer la he puesto aqui para que no se destruya ningun mensaje
            v_aux = new Vector();
            msg_data = new Vector();
        } catch (IOException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return (clientSocket != null);
    }

    protected void downloadFile(int objectSelected, String path) {
        //SOLICITAR DESCARGAR UN IMAGEN
        v_aux.clear();
        v_aux.add(files_available.get(objectSelected).getId());
        buildMessage(DOWNLOAD_FILE, v_aux, 1);
        takeFile(files_available.get(objectSelected).getFullName(), path);
        v_aux.clear();
        v_aux.add("ACK");
        buildMessage(DOWNLOAD_FILE,v_aux,1);
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

    protected void searchFileBy(String cadena){
        v_aux.clear();
        v_aux.add(cadena);
        buildMessage(GET_FILE, v_aux, 1);
        readMessage();
        int data_size = 0;
        if(msg_code.equals(SEND_FILE_SIZE))
            data_size = Integer.parseInt(msg_data.get(0));
        String name,id;
        long size;
        
        if(files_available == null)
            files_available = new Vector();//si esto no esta aqui al desconectar y volver a conectar conserva los elementos de antes, quedan repetidos
        else
            files_available.clear();
        
        for(int i=0;i<data_size;i++){
            readMessage();
            if(msg_code.equals(SEND_FILE_DATA)){
                name = msg_data.get(0);
                id = msg_data.get(1);
                size = Long.parseLong(msg_data.get(2));
                mFile = new MFile("unknow",name, id,size);
                files_available.add(mFile);
            }
        }
    }

    private void writeMessage(String message){
        try {
            outWriter = new PrintWriter(clientSocket.getOutputStream(),true);
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
    
    private void takeFile(String name, String path){
        try{
            ois = new ObjectInputStream(clientSocket.getInputStream());
            Object o = ois.readObject();
            if(o instanceof byte[])
                buffer= (byte[])o;
            fos = new FileOutputStream(path + name);
            fos.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(cliente_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void endConnection() {
        v_aux.clear();
        v_aux.add("END_CONNECTION");
        buildMessage(END_CONNECTION, v_aux, 1);
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
}
