/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imos.abos.forms;

/**
 *
 * @author Peter Jansen <peter.jansen@utas.edu.au>
 */
interface DataProcessor
{
    public String paramToString();

    public boolean setupFromString(String s);
    
    public void calculateDataValues();
}
