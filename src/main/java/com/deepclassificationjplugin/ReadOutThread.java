package com.deepclassificationjplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adines
 */
public class ReadOutThread extends Thread{
    private BufferedReader stdOut;
    private String salida;
    
    public ReadOutThread(BufferedReader stdOut){
        super();
        this.stdOut=stdOut;
    }
    
    @Override
    public void run(){
        try {
            String line;
            while((line=stdOut.readLine())!=null){
                salida=line;
            }
        } catch (IOException ex) {
            Logger.getLogger(ReadErrorThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getLinea(){
        return this.salida;
    }
}
