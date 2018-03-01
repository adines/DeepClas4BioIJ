/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.deepclassificationjplugin;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author adines
 */
public class FilenameFilterFramework implements FilenameFilter{

    String framework;
    public FilenameFilterFramework(String framwork){
        super();
        this.framework=framwork;
    }
    
    @Override
    public boolean accept(File dir, String name) {
        if(name.startsWith(framework)&&name.endsWith("ModelClassification.py")&&!name.equalsIgnoreCase(framework+"ModelClassification.py")){
            return true;
        }else{
            return false;
        }
    }
    
}
