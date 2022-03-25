/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.swing.*;

/**
 *
 * @author yaqiang
 */
public class WContourShanghaiDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        FrmShanghaiMain aFrm = new FrmShanghaiMain();
        aFrm.setVisible(true);
    }
}
