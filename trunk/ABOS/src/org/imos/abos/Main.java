/*
 * Neonatal Screening Software Project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos;

import org.wiley.core.startMenu;
import org.wiley.util.PasswordDialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.TimeZone;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wiley.core.Common;
import org.wiley.core.dbms.Staff;

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
            UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
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

        if(args.length >= 2)
        {
            PropertyConfigurator.configure(args[1]);
        }
        else
        {
            PropertyConfigurator.configure(home + "/ABOS/log4j.properties");
        }
        if (args.length > 0)
        {
            //
            // passed in the database connection parameters
            //
            Common.closeConnection();
            Common.build( args[0]);
        }
        else
        {
            Common.closeConnection();
            Common.build(home + "/ABOS/ABOS.conf");
        }

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        
        startMenu mm = new startMenu();

        mm.setUDPBroadcasts(false);
        mm.setSize(640,450);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = mm.getSize();

        mm.setLocation( (screenSize.width - frameSize.width) / 2,
                         (screenSize.height - frameSize.height) / 2);

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
                //
                // check password
                //
                String storedPassword = currentUser.getPassword();

                if(storedPassword.equals(Common.getUserPassword()))
                {
                    Common.setCurrentStaffRecord(currentUser);
                }
                else
                {
//                    Common.showMessage(mm,
//                        "Incorrect Password",
//                        "The password entered for login ID "
//                        + Common.getDBMSUser()
//                        + " does not match the stored value."
//                        );

                    PasswordDialog pd = new PasswordDialog(mm, true);
                    pd.setLocationRelativeTo(mm);
                    pd.setVisible(true);

                    char[] pwd = pd.getPassword();
                    String pwd2 = new String(pwd);

                    //System.out.println("Entered password was " + pwd2);
                    //System.out.println("Stored password was " + storedPassword);
                    if(storedPassword.equals(pwd2))
                    {
                        Common.setCurrentStaffRecord(currentUser);
                    }
                    else
                    {
                        Common.showMessage(mm,
                        "Incorrect Password",
                        "The override password entered for login ID "
                        + Common.getDBMSUser()
                        + " does not match the stored value."
                        );
                        System.exit(-1);
                    }
                }
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
        }
    }

}
