/*
 * IMOS data delivery project
 * Written by Peter Wiley
 * This code is copyright (c) Peter Wiley 2000 - ?
 * It is made available under the BSD Software Licence in the hope that it may be useful.
 * It has NO WARRANTY OF FITNESS OR SUITABILITY FOR ANY PURPOSE.
 * Feel free to fix any bugs that you may find.
 */

package org.imos.dwm.dbms.fields;

/**
 *
 * @author peter
 */
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.imos.dwm.dbms.Instrument;
import org.imos.dwm.dbms.Mooring;
import org.wiley.util.BasicCombo;
import org.wiley.util.basicField;

public class InstrumentSelectorCombo extends BasicCombo
{

    private static Logger logger = Logger.getLogger( InstrumentSelectorCombo.class.getName());
    JComboBox combo = null;

    private basicField descriptionField = null;

    ArrayList<Instrument> codes = null;
    private Instrument selected = null;

    public InstrumentSelectorCombo()
    {
        super();
        setLabel("Instrument");

        combo = new JComboBox();

        codes = Instrument.selectAllActiveCodes();

        if(codes != null && codes.size() > 0)
        {
//            combo.addItem("");
//
//            for(int i = 0; i < codes.size(); i++)
//            {
//                Instrument ps = codes.get(i);
//                combo.addItem(ps.getInstrumentID());
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
                        setDescriptionText("");
                    }
                    else
                    {
                        selected =  codes.get(index);
                        if(selected != null)
                        {
                            //logger.debug("Selected " + selected.getDescription() );
                            setDescriptionText(selected.getInstrumentID()
                                                + ": "
                                                + selected.getMake()
                                                + "/"
                                                + selected.getModel()
                                                + "/"
                                                + selected.getSerialNumber()
                                            );
                            
                            firePropertyChange("INSTRUMENT_SELECTED", null, selected);
                        }
                        else
                        {
                            logger.debug("Selected item is null");
                            setDescriptionText("");
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
        setDescriptionText("");
    }

    public void setDescriptionField(basicField field)
    {
        descriptionField = field;
    }

    private void setDescriptionText(String text)
    {
        if(descriptionField != null)
        {
            descriptionField.setText(text);
        }
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
    public Instrument getSelectedInstrument()
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

    public void setMooring(Mooring selectedItem)
    {
        DecimalFormat df = new DecimalFormat("0000");
        combo.removeAllItems();

        codes = Instrument.selectInstrumentsForMooring(selectedItem.getMooringID());

        if(codes != null && codes.size() > 0)
        {
            combo.addItem("");

            for(int i = 0; i < codes.size(); i++)
            {
                Instrument ps = codes.get(i);
                combo.addItem(df.format(ps.getInstrumentID())
                            + ": "
                            + ps.getMake()
                            + " "
                            + ps.getModel()
                            + " S/No. "
                            + ps.getSerialNumber()
                            );
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

    public void setMooringParam(Mooring selectedItem, String param)
    {
        DecimalFormat df = new DecimalFormat("0000");
        combo.removeAllItems();

        codes = Instrument.selectForRawData(selectedItem.getMooringID(), param);

        if(codes != null && codes.size() > 0)
        {
            combo.addItem("");

            for(int i = 0; i < codes.size(); i++)
            {
                Instrument ps = codes.get(i);
                combo.addItem(df.format(ps.getInstrumentID())
                            + ": "
                            + ps.getMake()
                            + " "
                            + ps.getModel()
                            + " S/No. "
                            + ps.getSerialNumber()
                            );
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
    public void setInstrumentDataSet(ArrayList<Instrument> set)
    {
        codes = set;
        
        combo.removeAllItems();

        if(codes != null && codes.size() > 0)
        {
            combo.addItem("");

            for(int i = 0; i < codes.size(); i++)
            {
                Instrument ps = codes.get(i);
                combo.addItem(ps.getInstrumentID()
                            + ": "
                            + ps.getMake()
                            + " "
                            + ps.getModel()
                            + " S/No. "
                            + ps.getSerialNumber()
                            );
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
