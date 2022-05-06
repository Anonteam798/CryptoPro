package controller;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import main.ThreadPoolClass;
import model.AES;
import util.ModelListItems;
import view.FrmMain;

/**
 *
 * @author h4ck3r
 */
public class MainController implements ActionListener, MouseListener {

    private FrmMain objFrmMain;
    private AES objAes;
    private ModelListItems objModelList;
    private File selectedFile;
    private boolean selectedAllItems = false;
    private boolean selectedOneFile = false;
    private int currentIndexOneFileChoosed;
    private int counter = 0;
    private boolean passwordShown = false;
    private boolean isKeyLoaded = false;
    private Key keyLoaded;

    public MainController(FrmMain objFrmMain) {
        this.objFrmMain = objFrmMain;
        objModelList = new ModelListItems();
        this.objAes = new AES();

        this.objFrmMain.btnSavePassword.addActionListener(this);
        this.objFrmMain.btnLoadPassword.addActionListener(this);
        this.objFrmMain.btnMosContraseña.addActionListener(this);
        this.objFrmMain.btnEscogerTodos.addActionListener(this);
        this.objFrmMain.btnDecifrarF.addActionListener(this);
        this.objFrmMain.btnCifrarF.addActionListener(this);
        this.objFrmMain.btnEscogerFicheros.addActionListener(this);

        /*Add the model to the JList*/
        this.objFrmMain.lstFicheros.setModel(objModelList);
        this.objFrmMain.lstFicheros.addMouseListener(this);

    }

    public void runApp() {
        FlatArcDarkIJTheme.setup();
        System.setProperty("flatlaf.useUbuntuFont", "true");
        this.objFrmMain.setVisible(true);
        this.objFrmMain.setLocationRelativeTo(null);

    }

    void notifyAboutMissingJars() {
        String jtattooJar = "JTattoo-1.6.13.jar";
        //String flatlafJar = "flatlaf-2.2.jar";
        JOptionPane.showMessageDialog(objFrmMain, "Ficheros Jar no encontrados: \n" + jtattooJar,
                objFrmMain.getTitle(), JOptionPane.ERROR_MESSAGE);

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == objFrmMain.btnCifrarF) {
            runCipherInThread();
        }
        if (ae.getSource() == objFrmMain.btnDecifrarF) {
            runDecipherInThread();
        }
        if (ae.getSource() == objFrmMain.btnEscogerTodos) {
            chooseAllFiles();
        }
        if (ae.getSource() == objFrmMain.btnEscogerFicheros) {
            loadFiles();
        }
        if (ae.getSource() == objFrmMain.btnMosContraseña) {
            showPassword();
        }
        if (ae.getSource() == objFrmMain.btnLoadPassword) {
            loadKey();
        }
        if (ae.getSource() == objFrmMain.btnSavePassword) {
            ThreadPoolClass.getThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    saveKey();
                }
            });
        }

    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getSource() == objFrmMain.lstFicheros) {
            int index;

            index = objFrmMain.lstFicheros.locationToIndex(me.getPoint());
            currentIndexOneFileChoosed = index;
            if (index >= 0) {
                selectedFile = new File(objFrmMain.lstFicheros.getModel().getElementAt(index));

                String weight = calculateFileSize(selectedFile.length());
                String creation = getCreationTime(selectedFile);
                objFrmMain.txtPesoF.setText(weight);
                objFrmMain.txtFCreado.setText(creation);
                selectedOneFile = true;
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    private String calculateFileSize(long byteSize) {
        String format = "";
        double converted;
        if (byteSize > 1000000) {
            //De bytes a mega bytes se hace una doble division 
            converted = Math.round(byteSize / 1024 /1024);
            format = String.format("%.2f Mb", converted);
        }
        else if (byteSize > 1000 && byteSize < 999999) {
            converted = Math.round(byteSize / 1024);
            format = String.format("%.2f Kb", converted);
        } else {
            //Apply with bytes to kilobtes;

            format = byteSize + " b";
        }
        return format;
    }

    private String getCreationTime(File f) {
        String time = null;
        try {
            FileTime ft = (FileTime) Files.getAttribute(Path.of(f.getAbsolutePath()), "creationTime");
            LocalDateTime lc = ft.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            time = lc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return time;
    }

    private void showPassword() {
        if (passwordShown) {
            this.objFrmMain.txtContraseña.setEchoChar('*');
            passwordShown = false;
        } else {
            this.objFrmMain.txtContraseña.setEchoChar((char) 0);
            passwordShown = true;
        }
    }

    private void controlsClean() {
        objFrmMain.txtContraseña.setText("");
    }

    private void blockControls(boolean act) {
        objFrmMain.txtContraseña.setEnabled(act);
        objFrmMain.txtContraseña.setBackground(Color.WHITE);
    }

    private boolean isPasswordEmpty(char[] pass) {
        boolean is = false;
        if (pass.length == 0) {
            is = true;
        }

        return is;
    }

    private void saveKey() {

        try {
            String plainPassword = Arrays.toString(objFrmMain.txtContraseña.getPassword());
            if (!isPasswordEmpty(plainPassword.toCharArray())) {
                objFrmMain.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int choosed = objFrmMain.chooser.showSaveDialog(objFrmMain);
                if (choosed == JFileChooser.APPROVE_OPTION) {
                    File filePath = objFrmMain.chooser.getSelectedFile();

                    if (objAes.saveKeyInFile(filePath.getAbsolutePath(),
                            objAes.generateSymetricKey(plainPassword))) {
                        JOptionPane.showMessageDialog(objFrmMain, "Contraseña guardada satisfactoriamente", objFrmMain.getTitle(),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    controlsClean();

                }
            } else {
                JOptionPane.showMessageDialog(objFrmMain, "Porfavor introduce una contraseña",
                        objFrmMain.getTitle(), JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadKey() {
        blockControls(false);
        objFrmMain.chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        objFrmMain.chooser.setFileFilter(new FileNameExtensionFilter("key", "txt"));
        int opened = objFrmMain.chooser.showOpenDialog(objFrmMain);
        if (opened == JFileChooser.APPROVE_OPTION) {
            keyLoaded = objAes.loadKeyFromFile(objFrmMain.chooser.
                    getSelectedFile().getAbsolutePath());

            if (keyLoaded != null) {
                objFrmMain.txtContraseña.setText("Cargado con exito");
                isKeyLoaded = true;
            } else {

                JOptionPane.showMessageDialog(objFrmMain, "Fichero incorrecto",
                        objFrmMain.getTitle(), JOptionPane.WARNING_MESSAGE);
                blockControls(true);
            }

        } else {
            blockControls(true);
        }
    }

    private void loadFiles() {
        objFrmMain.chooser.setMultiSelectionEnabled(true);
        objFrmMain.chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int aprove = objFrmMain.chooser.showOpenDialog(objFrmMain);

        if (aprove == JFileChooser.APPROVE_OPTION) {
            File[] files = objFrmMain.chooser.getSelectedFiles();
            for (File file : files) {
                objModelList.addFiles(file);
            }

        }

    }

    private void runCipherInThread() {
        Runnable run = () -> {
            cipherFiles();
            System.out.println("Cifrando en el hilo: " + Thread.currentThread().getName());
        };
        ThreadPoolClass.getThreadPool().submit(run);
    }

    private void runDecipherInThread() {
        Runnable run = () -> {
            decipherFiles();
            
        };
        ThreadPoolClass.getThreadPool().submit(run);
    }

    private void cipherFiles() {
        
        counter = 0;
        Key keyToCipherKey = keyLoaded;
        String plainPassword = Arrays.toString(objFrmMain.txtContraseña.getPassword());
        if (!isPasswordEmpty(plainPassword.toCharArray())) {
            if (!isKeyLoaded) {
                keyToCipherKey = objAes.generateSymetricKey(plainPassword);
            }
            if (selectedAllItems) {

                while (objModelList.getSize() != 0) {
                    try {
                        Thread.sleep(1000L);
                        File fileTmp = new File(objFrmMain.lstFicheros.getModel().getElementAt(0));

                        byte[] encryptedData = objAes.encrypt(
                                objAes.readBytesFromFile(fileTmp), keyToCipherKey);
                        objAes.storeFile(encryptedData, fileTmp.getAbsolutePath(), objFrmMain.pgbProgreso);
                        objModelList.removeElementAt(0);
                        printWorkedFiles(1);
                        
                        
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                selectedAllItems = false;

            } else if (selectedOneFile) {
                byte[] encryptedData = objAes.encrypt(
                        objAes.readBytesFromFile(selectedFile), keyToCipherKey);
                objAes.storeFile(encryptedData, selectedFile.getAbsolutePath(), objFrmMain.pgbProgreso);
                printWorkedFiles(1);
                objModelList.removeElementAt(currentIndexOneFileChoosed);
                
             

                selectedOneFile = false;
            } else {
                JOptionPane.showMessageDialog(objFrmMain, "Selecciona al menos un fichero para cifrar",
                        objFrmMain.getTitle(), JOptionPane.WARNING_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(objFrmMain, "No has definido o cargado una contraseña",
                    objFrmMain.getTitle(), JOptionPane.WARNING_MESSAGE);
        }
        restoreKeys();

    }

    private void decipherFiles() {
        counter = 0;
        Key keyToDecipher = keyLoaded;
        String plainPassword = Arrays.toString(objFrmMain.txtContraseña.getPassword());
        if (!isPasswordEmpty(plainPassword.toCharArray())) {

            if (!isKeyLoaded) {
                keyToDecipher = objAes.generateSymetricKey(plainPassword);
            }

            if (selectedAllItems) {
                while (objModelList.getSize() != 0) {
                    try {
                        Thread.sleep(1000L);
                        File fileTmp = new File(objFrmMain.lstFicheros.getModel().getElementAt(0));
                        byte[] decryptedData = objAes.decrypt(objAes.readBytesFromFile(fileTmp), keyToDecipher);
                        objAes.storeFile(decryptedData, fileTmp.getAbsolutePath(), objFrmMain.pgbProgreso);
                        objModelList.removeElementAt(0);
                        printWorkedFiles(1);
                        
                        
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (BadPaddingException ex) {
                        JOptionPane.showMessageDialog(objFrmMain, "La contraseña posiblemente es la incorrecta", objFrmMain.getTitle(),
                                JOptionPane.WARNING_MESSAGE);
                    } catch (IllegalBlockSizeException ex) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                selectedAllItems = false;

            } else if (selectedOneFile) {
                byte[] decryptedData;
                try {
                    decryptedData = objAes.decrypt(
                            objAes.readBytesFromFile(selectedFile), keyToDecipher);
                    objAes.storeFile(decryptedData, selectedFile.getAbsolutePath(), objFrmMain.pgbProgreso);
                    printWorkedFiles(1);
                    objModelList.removeElementAt(currentIndexOneFileChoosed);
                    
                } catch (BadPaddingException ex) {
                    JOptionPane.showMessageDialog(objFrmMain, "La contraseña posiblemente es la incorrecta", objFrmMain.getTitle(),
                            JOptionPane.WARNING_MESSAGE);
                
                } catch (IllegalBlockSizeException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }

                selectedOneFile = false;
            } else {
                JOptionPane.showMessageDialog(objFrmMain, "Selecciona al menos un fichero para cifrar",
                        objFrmMain.getTitle(), JOptionPane.WARNING_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(objFrmMain, "No has definido o cargado una contraseña",
                    objFrmMain.getTitle(), JOptionPane.WARNING_MESSAGE);
        }
        restoreKeys();

    }

    private void chooseAllFiles() {

        objFrmMain.lstFicheros.setSelectionInterval(0, objModelList.getSize());

        objFrmMain.lstFicheros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selectedAllItems = true;
        
        long totalWeight = 0;
        File tempFile;
        /*Calculando el peso de todos los ficheros seleccionados*/
        for (int i = 0; i < objModelList.getSize(); i++) {
            tempFile = new File(objModelList.getElementAt(i));
            totalWeight += tempFile.length();
            
        }
        
        String calculatedWeight = calculateFileSize(totalWeight);
        objFrmMain.txtPesoF.setText(calculatedWeight);
        objFrmMain.txtFCreado.setText("Many Dates");
        
    }

    private void printWorkedFiles(int iter) {

        String text = (counter += iter) + " Ficheros trabajados";
        objFrmMain.lblNotificador.setText(text);
        objFrmMain.txtFCreado.setText("");
        objFrmMain.txtPesoF.setText("");

    }

    private void restoreKeys() {
        objFrmMain.txtContraseña.setEnabled(true);
        keyLoaded = null;
        isKeyLoaded = false;
        controlsClean();
        
        
        
    }

}
