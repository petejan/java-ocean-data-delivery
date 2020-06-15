/*
 * Copyright (c) 2015, jan079
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withSystem.out
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.imos.dwm.parsers.AZFP;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jan079
 */
public class ParseAZFPxml
{

    File file;
    Document doc;

    public ParseAZFPxml(String filename) throws ParserConfigurationException, SAXException, IOException
    {
        file = new File(filename);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(!true);

        doc = db.parse(new File(filename));
    }

    public String getText(Node node)
    {
        StringBuffer result = new StringBuffer();
        if (!node.hasChildNodes())
        {
            return "";
        }

        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE)
            {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() == Node.CDATA_SECTION_NODE)
            {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() == Node.ENTITY_REFERENCE_NODE)
            {
                // Recurse into the subtree for text
                // (and ignore comments)
                result.append(getText(subnode));
            }
        }

        return result.toString();
    }

    private void printlnCommon(Node n)
    {
        System.out.print(" nodeName=\"" + n.getNodeName() + "\"");

        String val = n.getNamespaceURI();
        if (val != null)
        {
            System.out.print(" uri=\"" + val + "\"");
        }

        val = n.getPrefix();

        if (val != null)
        {
            System.out.print(" pre=\"" + val + "\"");
        }

        val = n.getLocalName();
        if (val != null)
        {
            System.out.print(" local=\"" + val + "\"");
        }

        val = n.getNodeValue();
        if (val != null)
        {
            System.out.print(" nodeValue=");
            if (val.trim().equals(""))
            {
                // Whitespace
                System.out.print("[WS]");
            }
            else
            {
                System.out.print("\"" + n.getNodeValue() + "\"");
            }
        }
        System.out.println();
    }

    public void parse()
    {
//        for (Node child = doc.getFirstChild(); child != null; child = child.getNextSibling())
//        {
//            echo(child);
//        }
        
        NodeList nl = doc.getElementsByTagName("Analog_Temperature");
        for(int i=0;i<nl.getLength();i++)
        {
            Node n = nl.item(i);
            System.out.println("Node name " + n);
            echo(n);
            Element eElement = (Element) n;
            NodeList nl2 = eElement.getElementsByTagName("A");
            System.out.println("Node values:");
            for(int j=0;j<nl2.getLength();j++)
            {
                Node n2 = nl2.item(i);
                //if (nl2.item(i).getNodeName().compareTo("A") == 0)
                {
                    echo(n2);
                }
            }
        }
    }
    
    public double getAnalogTempA()
    {
        Element e = (Element)doc.getElementsByTagName("Analog_Temperature").item(0);
        Node n2 = e.getElementsByTagName("A").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getAnalogTempB()
    {
        Element e = (Element)doc.getElementsByTagName("Analog_Temperature").item(0);
        Node n2 = e.getElementsByTagName("B").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getAnalogTempC()
    {
        Element e = (Element)doc.getElementsByTagName("Analog_Temperature").item(0);
        Node n2 = e.getElementsByTagName("C").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getAnalogTempKA()
    {
        Element e = (Element)doc.getElementsByTagName("Analog_Temperature").item(0);
        Node n2 = e.getElementsByTagName("ka").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getAnalogTempKB()
    {
        Element e = (Element)doc.getElementsByTagName("Analog_Temperature").item(0);
        Node n2 = e.getElementsByTagName("kb").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getAnalogTempKC()
    {
        Element e = (Element)doc.getElementsByTagName("Analog_Temperature").item(0);
        Node n2 = e.getElementsByTagName("kc").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltXA()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("X_a").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltXB()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("X_b").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltXC()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("X_c").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltXD()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("X_d").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltYA()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("Y_a").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltYB()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("Y_b").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltYC()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("Y_c").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getTiltYD()
    {
        Element e = (Element)doc.getElementsByTagName("AG_Tilt").item(0);
        Node n2 = e.getElementsByTagName("Y_d").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getFrequencyTVR(int i)
    {
        Element e = (Element)doc.getElementsByTagName("Frequency").item(i);
        Node n2 = e.getElementsByTagName("TVR").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getFrequencyVTX(int i)
    {
        Element e = (Element)doc.getElementsByTagName("Frequency").item(i);
        Node n2 = e.getElementsByTagName("VTX0").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getFrequencyBP(int i)
    {
        Element e = (Element)doc.getElementsByTagName("Frequency").item(i);
        Node n2 = e.getElementsByTagName("BP").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getFrequencyDS(int i)
    {
        Element e = (Element)doc.getElementsByTagName("Frequency").item(i);
        Node n2 = e.getElementsByTagName("DS").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }
    public double getFrequencyEL(int i)
    {
        Element e = (Element)doc.getElementsByTagName("Frequency").item(i);
        Node n2 = e.getElementsByTagName("EL").item(0);
        
        return Double.parseDouble(n2.getTextContent());
    }

    int indent;
    
    private void outputIndentation()
    {
        for (int i = 0; i < indent; i++)
        {
            System.out.print("  ");
        }
    }
    private void echo(Node n)
    {
        int type = n.getNodeType();
        outputIndentation();

        switch (type)
        {
            case Node.ATTRIBUTE_NODE:
                System.out.print("ATTR:");
                printlnCommon(n);
                break;

            case Node.CDATA_SECTION_NODE:
                System.out.print("CDATA:");
                printlnCommon(n);
                break;

            case Node.COMMENT_NODE:
                System.out.print("COMM:");
                printlnCommon(n);
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
                System.out.print("DOC_FRAG:");
                printlnCommon(n);
                break;

            case Node.DOCUMENT_NODE:
                System.out.print("DOC:");
                printlnCommon(n);
                break;

            case Node.DOCUMENT_TYPE_NODE:
                System.out.print("DOC_TYPE:");
                printlnCommon(n);
                NamedNodeMap nodeMap = ((DocumentType) n).getEntities();
                indent += 2;
                for (int i = 0; i < nodeMap.getLength(); i++)
                {
                    Entity entity = (Entity) nodeMap.item(i);
                    echo(entity);
                }
                indent -= 2;
                break;

            case Node.ELEMENT_NODE:
                System.out.print("ELEM:");
                printlnCommon(n);

                NamedNodeMap atts = n.getAttributes();
                indent += 2;
                for (int i = 0; i < atts.getLength(); i++)
                {
                    Node att = atts.item(i);
                    echo(att);
                }
                indent -= 2;
                break;

            case Node.ENTITY_NODE:
                System.out.print("ENT:");
                printlnCommon(n);
                break;

            case Node.ENTITY_REFERENCE_NODE:
                System.out.print("ENT_REF:");
                printlnCommon(n);
                break;

            case Node.NOTATION_NODE:
                System.out.print("NOTATION:");
                printlnCommon(n);
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                System.out.print("PROC_INST:");
                printlnCommon(n);
                break;

            case Node.TEXT_NODE:
                System.out.print("TEXT:");
                printlnCommon(n);
                break;

            default:
                System.out.print("UNSUPPORTED NODE: " + type);
                printlnCommon(n);
                break;
        }

        indent++;
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling())
        {
            echo(child);
        }
        indent--;
    }

    public static void main(String[] args)
    {
        try
        {
            ParseAZFPxml ml = new ParseAZFPxml(args[0]);

            // ml.parse();
            
            System.out.println("Analog Temperature A = " + ml.getAnalogTempA());
            System.out.println("Tilt X A = " + ml.getTiltXA());
            System.out.println("Frequency 0 TVR = " + ml.getFrequencyTVR(0));
            
        }
        catch (ParserConfigurationException ex)
        {
            Logger.getLogger(ParseAZFPxml.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SAXException ex)
        {
            Logger.getLogger(ParseAZFPxml.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ParseAZFPxml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
