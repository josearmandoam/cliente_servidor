/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paquete;

import java.io.File;
import java.util.UUID;

/**
 *
 * @author jose_
 */
public class MFile {
    private String path;
    private String name;
    private String id;
    private long size;
    public MFile(String path, String name){
        this.path = path;
        this.name = name;
        this.id = generateId();
    }
    
    public MFile(String path, String name, String id){
        this.path = path;
        this.name = name;
        this.id = id;
    }
    
    public MFile(String path, String name, long size){
        this.path = path;
        this.name = name;
        this.id = generateId();
        this.size = size;
    }
    
    public MFile(String path, String name, String id, long size){
        this.path = path;
        this.name = name;
        this.id = id;
        this.size = size;
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
    
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullName() {
        return name;
    }
    
    public String getName(){
        return name.substring(0,name.indexOf("."));
    }

    public void setFullName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    
    public String getFormatedSize(){
        String fSize = "0B";
        long size = this.size;
        fSize = this.size + "KB";
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
    
    public String getFileExtension(){
        String extesion = name.substring(name.indexOf(".")+1,name.length());
        return extesion;
    }
    
    @Override
    public String toString(){
        return "Archivo:\n[Path="+ path + "] [Name="+name+"] [Id="+ id+"] [Type="+getFileExtension()+"] [Size="+getFormatedSize()+"]";
    }
}
