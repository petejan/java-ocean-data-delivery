package org.imos.abos.forms;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class EditDialog extends JDialog implements ActionListener
{
	private final JPanel contentPanel = new JPanel();
    JButton cancelButton;
    JButton okButton;
    private JButton btnDelete;

	public EditDialog(Vector<String>labels, Vector<Object>values)
	{
		getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(5, 5));
        
        JPanel panelLabels = new JPanel();
        {
                contentPanel.add(panelLabels, BorderLayout.WEST);
                panelLabels.setLayout(new BorderLayout(5, 5));
                {
                        JPanel contentLeft = new JPanel();
                        contentLeft.setLayout(new GridLayout(0, 1, 0, 0));
                        panelLabels.add(contentLeft, BorderLayout.WEST);
                    	for(String s: labels)
                    	{
                            JLabel lbl = new JLabel(s);
                            contentLeft.add(lbl);
                    	}
                }
        }
        
        JPanel contentCentre = new JPanel();
        contentPanel.add(contentCentre, BorderLayout.CENTER);
        contentCentre.setLayout(new GridLayout(0, 1, 0, 0));
        {
        	for(Object o: values)
        	{
                JTextField txtId = new JTextField();
                
                contentCentre.add(txtId);
                txtId.setColumns(40);
        		if (o != null)
        		{
        			txtId.setText(o.toString());
        		}
                txtId.setEditable(false);
        	}
        }
        contentPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JPanel buttonPane = new JPanel();
        buttonPane.setBorder(BorderFactory.createEtchedBorder());
        
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(buttonPane, BorderLayout.SOUTH);
        {
                btnDelete = new JButton("Delete");
                buttonPane.add(btnDelete);
                btnDelete.addActionListener(this);
        }
        {
                okButton = new JButton("Ok");
                okButton.setActionCommand("Ok");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(this);
        }
        {
                cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
                cancelButton.addActionListener(this);
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    setVisible(false); 
	    //dispose(); 
	}
}
