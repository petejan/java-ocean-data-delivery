/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.imos.abos.dbms.MooringTable;
import org.imos.abos.dbms.SQLtable;
import org.wiley.core.Common;
import org.wiley.core.dbms.Staff;
import org.wiley.core.startMenu;

/**
 *
 * @author peter
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    private static org.apache.log4j.Logger log = Logger.getLogger(Main.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
	    // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e)
        {
           // handle exception
        }
        catch (ClassNotFoundException e)
        {
           // handle exception
        }
        catch (InstantiationException e)
        {
           // handle exception
        }
        catch (IllegalAccessException e)
        {
           // handle exception
        }

        String home = System.getProperty("user.home");

        // configure log4j
        if(args.length >= 2)
        {
            PropertyConfigurator.configure(args[1]);
        }
        else
        {
            PropertyConfigurator.configure("log4j.properties");
        }
        if (args.length > 0)
        {
            //
            // passed in the database connection parameters
            //
            Common.closeConnection();
            Common.build( args[0] );
        }
        else
        {
            Common.closeConnection();
            Common.build("ABOS.properties");
        }
        log.debug("Common.getDBMSUser " + Common.getDBMSUser());

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        if (defaults.get("Table.alternateRowColor") == null)
            defaults.put("Table.alternateRowColor", new Color(224,255,255));
        
        startMenu mm = new startMenu();

        mm.setUDPBroadcasts(false);
        mm.setSize(1060, 650);

        mm.setLocationRelativeTo(null);

        mm.setTitle("ABOS Main Menu");
        mm.setVisible( true );
        
        if( Common.getDBMSUser().equalsIgnoreCase("NOBODY") )
        {
            //
            // args passed in require an overriding user name/password
            //
            org.wiley.dbConnection userData = new org.wiley.dbConnection( mm.getRootPane(), false );

            Common.setDBMSUser( userData.getUserName() );
            Common.setUserPassword( userData.getPassword() );

            userData = null;
        }
        if( Common.getConnection() == null)
        {
          Common.showMessage("No Database Connection","Failed to connect to database - terminating.");
          System.exit(-1);
        }
        else
        {
            //
            // got our database connection - initialise stuff
            //
            Staff currentUser = Staff.selectByID(Common.getDBMSUser());

            if(currentUser == null)
            {
                currentUser = Staff.selectByDBMSLoginID(Common.getDBMSUser());
                if(currentUser == null)
                {
                    Common.showMessage(mm,
                            "No Such Person",
                            "No person with login ID "
                            + Common.getDBMSUser()
                            + " exists in the system."
                            );
                    System.exit(-1);
                }
            }
            else
            {
                Common.setCurrentStaffRecord(currentUser);
                //
                // check password
                //
//                String storedPassword = currentUser.getPassword();
//
//                if(storedPassword.equals(Common.getUserPassword()))
//                {
//                    Common.setCurrentStaffRecord(currentUser);
//                }
//                else
//                {
////                    Common.showMessage(mm,
////                        "Incorrect Password",
////                        "The password entered for login ID "
////                        + Common.getDBMSUser()
////                        + " does not match the stored value."
////                        );
//
//                    PasswordDialog pd = new PasswordDialog(mm, true);
//                    pd.setLocationRelativeTo(mm);
//                    pd.setVisible(true);
//
//                    char[] pwd = pd.getPassword();
//                    String pwd2 = new String(pwd);
//
//                    //System.out.println("Entered password was " + pwd2);
//                    //System.out.println("Stored password was " + storedPassword);
//                    if(storedPassword.equals(pwd2))
//                    {
//                        Common.setCurrentStaffRecord(currentUser);
//                    }
//                    else
//                    {
//                        Common.showMessage(mm,
//                        "Incorrect Password",
//                        "The override password entered for login ID "
//                        + Common.getDBMSUser()
//                        + " does not match the stored value."
//                        );
//                        System.exit(-1);
//                    }
//                }
            }
            mm.setTitle(mm.getTitle()
                      + " - "
                      + Common.getCurrentDBMS()
                      + " on "
                      + Common.getCurrentHost()
                      + " as "
                      + Common.getDBMSUser());
          //
          // load up the menus
          //
          mm.buildMenus();
//          Dimension s = mm.getSize();
//          MooringTable table = new MooringTable();
//          table.initialise();
//          table.setSize(s.width-100, s.height-100);
          
//          SQLtable tab;
//          try
//          {
//        	  tab = new SQLtable(Common.dbURL, "SELECT * FROM mooring");
//        	  JTable table = tab.getTable();
//        	  //mm.addComponent(new JScrollPane(table));
//          }
//          catch (Exception e)
//          {
//        	  // TODO Auto-generated catch block
//        	  e.printStackTrace();
//          }


        }
    }

}
