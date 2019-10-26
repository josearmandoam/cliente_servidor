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
    
    @Override
    public String toString(){
        return "Archivo:\n[Path="+ path + "] [Name="+name+"] [Id="+ id+"]";
    }
}
