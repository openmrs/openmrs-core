/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.serialization;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.CDATASection;

/* An container class to hide XML details
 * from the user
 *
 * @todo some of the node<->element stuff is not so pretty
 */
public class Item
{
    private Node m_node;

    /** direct access to some obvious features
     */
    public String getAttribute(String sName)
    {
        return getElement().getAttribute(sName);
    }

	public void setAttribute(String sName, String sValue)
		throws Exception
	{
		getElement().setAttribute(sName, sValue);
	}

	public void removeAttribute(String sName)
		throws Exception
	{
		getElement().removeAttribute(sName);
	}
    
    public boolean isEmpty()
    {
        return (!m_node.hasChildNodes() && !m_node.hasAttributes()) ;
    }

    /** Retrieve the content of an item created via
     * xml_serializer.createText(Item parent, String data) *or* 
     * xml_serializer.createTextAsCDATA(Item parent, String data) 
     */
	public String getText()
	{
        try {
            return (String)getData();
        }
        catch (Exception e) {
            //e.printStackTrace();
            return Record.NULLSTR;
        }
	}

    /** Retrieve the text content of an item which
     * contains cdata/text
     */
	public String getData()
	{
        Node enode = getNode();
		StringBuffer data = new StringBuffer();
		NodeList cdata = enode.getChildNodes();

		// expect just 1
		int sz = cdata.getLength();
		for (int j = 0; j < sz; j++)
		{
			Node node = cdata.item(j);
			if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE)
			{
				Text sec = (Text)node; //note: CDATASection is derived from Text
				sec.normalize();
				data.append(sec.getData().trim());
			}
		}

		return data.toString();
	}

    /** Return the number of child items for this item
     */
    public int numItems()
    {
        NodeList nodes = getNode().getChildNodes();
		return nodes.getLength();
    }


    public Item() {}
    public Item(Element e) {m_node = (Node)e;}
    public Item(Node n) {m_node = n;}
    public final void setElement(Element e) {m_node = (Node)e;}
    public final Element getElement() {return (Element)m_node;}
    public final Node getNode() {return m_node;}
}