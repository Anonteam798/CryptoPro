
package main;

import controller.MainController;
import view.FrmMain;

/**
 *
 * @author h4ck3r
 */
public class MainProgram {
    public static void main(String[] args) {
        FrmMain objMain = new FrmMain();
        MainController objMainController = new MainController(objMain);
        objMainController.runApp();
        
        
        
    }
    
}
