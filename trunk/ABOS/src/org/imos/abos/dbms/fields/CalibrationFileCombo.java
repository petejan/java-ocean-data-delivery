/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.abos.dbms.fields;

/**
 *
 * @author peter
 */
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.InstrumentCalibrationFile;
import org.imos.abos.dbms.Mooring;
import org.wiley.util.BasicCombo;

public class CalibrationFileCombo extends BasicCombo implements PropertyChangeListener
{

    private static Logger logger = Logger.getLogger( CalibrationFileCombo.class.getName());
    JComboBox combo = null;

    ArrayList<InstrumentCalibrationFile> codes = null;
    private InstrumentCalibrationFile selected = null;

    public CalibrationFileCombo()
    {
        super();
        setLabel("Calibration File");

        combo = new JComboBox();

        codes = InstrumentCalibrationFile.selectFilesWithCalibrationValues();

        if(codes != null && codes.size() > 0)
        {
//            combo.addItem("");
//
//            for(int i = 0; i < codes.size(); i++)
//            {
//                InstrumentCalibrationFile ps = codes.get(i);
//                combo.addItem(ps.getFileName());
//            }
        }
        else
        {
            combo.addItem("");

        }

        ItemListener il = new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                //logger.debug("Item state change");

                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    //logger.debug("Item selected");
                    int index = combo.getSelectedIndex() - 1;

                    if(index < 0)
                    {
                        logger.debug("Selected blank row");
                    }
                    else
                    {
                        selected =  codes.get(index);
                        if(selected != null)
                        {
                            //logger.debug("Selected " + selected.getDescription() );
                        }
                        else
                        {
                            logger.debug("Selected item is null");
                        }
                    }
                }
            }

        };

        combo.addItemListener(il);
        setDataField( combo );

        this.setPreferredSize(new Dimension(60,36));
    }

    public void clearField()
    {
        combo.setSelectedIndex(0);
    }

    @Override
    public int getSelectedIndex()
    {
        return combo.getSelectedIndex();
    }

    /**
     * get the mooring as selected in the combo box
     * @return
     */
    public InstrumentCalibrationFile getSelectedFile()
    {
        int row = combo.getSelectedIndex();
        if(row > 0)
            return codes.get(row - 1);
        else
            return null;
    }

    public void setSelectedItem( String item )
    {
        //logger.debug("Setting selection to " + item);
        combo.setSelectedItem( item );
    }

    public void setEditable( boolean isEditable )
    {
        //combo.setEditable( isEditable );
        setEnabled(isEditable);
    }

    @Override
    public void setEnabled(boolean isEditable)
    {
        combo.setEnabled(isEditable);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        String propertyName = evt.getPropertyName();

        if (propertyName.equals("MOORING_SELECTED"))
        {
            logger.debug(evt.getPropertyName());
        }
    }

    public void setMooring(Mooring selectedItem)
    {
        combo.removeAllItems();

        codes = InstrumentCalibrationFile.selectFilesWithCalibrationValuesForMooring(selectedItem);

        if(codes != null && codes.size() > 0)
        {
            combo.addItem("");

            for(int i = 0; i < codes.size(); i++)
            {
                InstrumentCalibrationFile ps = codes.get(i);
                combo.addItem(ps.getFileName());
            }
        }
        else
        {
            combo.addItem("");

        }

        ItemListener il = new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                //logger.debug("Item state change");

                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    //logger.debug("Item selected");
                    int index = combo.getSelectedIndex() - 1;

                    if(index < 0)
                    {
                        logger.debug("Selected blank row");
                    }
                    else
                    {
                        selected =  codes.get(index);
                        if(selected != null)
                        {
                            //logger.debug("Selected " + selected.getDescription() );
                        }
                        else
                        {
                            logger.debug("Selected item is null");
                        }
                    }
                }
            }

        };

        combo.addItemListener(il);
    }
}
