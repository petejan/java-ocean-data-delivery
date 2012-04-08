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
import java.util.ArrayList;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.imos.abos.dbms.InstrumentDataParser;
import org.wiley.util.BasicCombo;
import org.wiley.util.basicField;

public class InstrumentDataParserCombo extends BasicCombo
{

    private static Logger logger = Logger.getLogger( InstrumentDataParserCombo.class.getName());
    JComboBox combo = null;

    private basicField descriptionField = null;

    ArrayList<InstrumentDataParser> codes = null;
    private InstrumentDataParser selected = null;

    public InstrumentDataParserCombo()
    {
        super();
        setLabel("Parser");

        combo = new JComboBox();

        codes = InstrumentDataParser.selectAllActiveCodes();

        if(codes != null && codes.size() > 0)
        {
            combo.addItem("");

            for(int i = 0; i < codes.size(); i++)
            {
                InstrumentDataParser ps = codes.get(i);
                combo.addItem(ps.getParserClassName());
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
                        setDescriptionText("");
                    }
                    else
                    {
                        selected =  codes.get(index);
                        if(selected != null)
                        {
                            //logger.debug("Selected " + selected.getDescription() );
                            setDescriptionText(selected.getDescription());
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
    public InstrumentDataParser getSelectedItem()
    {
        int idx = combo.getSelectedIndex();
        return codes.get(idx - 1);
    }

    @Override
    public int getSelectedIndex()
    {
        return combo.getSelectedIndex();
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
}
