/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paquete;

import java.util.UUID;

/**
 *
 * @author jose_
 */
public class MFile {
    private String path;
    private String name;
    private String id;
    private String type;
    private String size;
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
    
    public MFile(String path, String name, String type, String size){
        this.path = path;
        this.name = name;
        this.id = generateId();
        this.type = type;
        this.size = size;
    }
    
    public MFile(String path, String name, String id, String type, String size){
        this.path = path;
        this.name = name;
        this.id = id;
        this.type = type;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
    
    
    
    @Override
    public String toString(){
        return "Archivo:\n[Path="+ path + "] [Name="+name+"] [Id="+ id+"] [Type="+type+"] [Size="+size+"]";
    }
}
