/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import javax.swing.DefaultListModel;

/**
 *
 * @author h4ck3r
 */
import java.io.File;
public class ModelListItems  extends DefaultListModel<String>{
    private int index = 0;

    public ModelListItems() {
    }
    
    public void addFiles(File files){
        
        addElement(files.getAbsolutePath());
        
    }
    public void resetIndex(){
        index = 0;
    }
    
}
