package com.deepclassificationjplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adines
 */
public class ReadErrorThread extends Thread{
    private InputStream stdError;
    
    public ReadErrorThread(InputStream stderror){
        super();
        this.stdError=stderror;
    }
    
    @Override
    public void run(){
        try {
            while(stdError.read()!=-1){
                
            }
        } catch (IOException ex) {
            Logger.getLogger(ReadErrorThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
