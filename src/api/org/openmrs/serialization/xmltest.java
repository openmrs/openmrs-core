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

import java.util.*;


abstract class thing
{
    protected int m_id;
    protected String m_stuff;

    public void setStuff(String s) {m_stuff = s;}
    public String getStuff(){return m_stuff;}

    protected abstract String getName();

    protected thing()
    {
        m_id = (int)(genID() & 0xffff);
    }

	public Item save(Record xml, Item parent) throws Exception
    {
        // some stuff for illustration only
		Item me = xml.createItem(parent, getName());
        xml.setAttribute(me, "id", Integer.toString(m_id));
        return me;
    }

	public void load(Record xml, Item me) throws Exception
    {
        // note this lame example overwrites the id from the constructor
		m_id = Integer.parseInt(me.getAttribute("id"));
    }

    // don't look here
    private static String lock = new String();
    private static long uniqueID=0;
	private long genID()
	{
        synchronized (lock){
            long t = System.currentTimeMillis();            
            while (t <= uniqueID) { t++; }
            return (uniqueID = t);
        }
	}

    public abstract String sayHi();
}

class parent extends thing
{
    ArrayList <child> m_children = new ArrayList<child>();
    protected String getName() {return "parent";}

    public int kids() {return m_children.size();}

    public String sayHi()
    {
        return getName() + "(" + m_id + ") has " + kids() + " kids, stuff is " + m_stuff;
    }

    public child getChild(int index) {return m_children.get(index);}

    public parent(int children)
    {
        for (int i = 0; i < children; i++)
        {
            child c = new child();
            m_children.add(c);
        }
    }

	public Item save(Record xml, Item parent) throws Exception
    {
        Item me = super.save(xml,parent);
        Item txt = xml.createText(me, m_stuff);

        for (int i = 0; i < m_children.size(); i++)
        {
            child c = m_children.get(i);
            c.save(xml, me);
        }

        return me;
    }

	public void load(Record xml, Item me) throws Exception
    {
		super.load(xml, me);
        m_stuff = me.getText();
        
        ArrayList <Item>children = xml.getItems(me, "child");
        for (int i = 0; i < children.size(); i++)
        {
            Item c = children.get(i);
            child theChild = new child();
            theChild.load(xml, c);
            m_children.add(theChild);
        }
    }

}

class child extends thing
{
    protected String getName() {return "child";}
    String m_birthday;

    public child()
    {
        m_birthday = new java.util.Date().toString();
    }

    public String sayHi()
    {
        return getName() + "(" + m_id + ") was born " + m_birthday;
    }

	public Item save(Record xml, Item parent) throws Exception
    {
        Item me = super.save(xml,parent);
        me.setAttribute("birthday", m_birthday);

        return me;
    }

	public void load(Record xml, Item me) throws Exception
    {
		super.load(xml, me);
        m_birthday = me.getAttribute("birthday");
        
    }
}
public class xmltest
{
    static final String root = "my_family";
    static final String root2 = "my_family2";

    static String stuff = 
        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" +
        "<" + root2 + ">" +
        "<gizmo> <junk> unused stuff</junk> </gizmo>" +
        "<gizmo/> <junk> unused stuff</junk>" +

        "<parent id='1'>Something<child id='1' birthday='Someday in May'/></parent>"+

        "<gizmo> <junk> unused stuff</junk> </gizmo>" +
        "<gizmo/> <junk> unused stuff</junk>" +

        "<parent id='2'>Something else" +
          "<child id='1' birthday='Someday in May'/>" +
          "<child id='2' birthday='Someday in June'/>" +
        "</parent>"+

        "<gizmo/> <junk> unused stuff</junk>" +
        "<gizmo> <junk> unused stuff</junk> </gizmo>" +
        "</" + root2 + ">"
        ;

    public static void main(String argv[]) 
    {
        System.out.println("Creating arbitrary parents");
        Package pkg=null;

        int hack = 10;
        ArrayList <parent> theParents = new ArrayList<parent>();

        for (int i = 0; i < hack; i++)
        {
            parent p = new parent(hack);
            p.setStuff("I am parent " + (i+1));
            theParents.add(p);
        }

        try {
            pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite(root);

            Item top = xml.getRootItem();
            for (int i = 0; i < hack; i++)
            {
                theParents.get(i).save(xml, top);
            }

            System.out.println("New doc is :" + xml.toString());           
        }
        catch (Exception e) {
            System.err.println("Oh well");
            e.printStackTrace();
        }

        System.out.println("Parsing the static stuff ");
        try {
            if (pkg==null){
                pkg = new FilePackage();
            }
            Record xml = pkg.createRecordFromString(stuff);
            Item top = xml.getItem(root2);
            if (top!=null) // for form, we know it's here
            {
                System.out.println("Record name is " + xml.getName());
                List <Item>gizmos = xml.getItems(top, "gizmo");
                System.out.println("Found " + gizmos.size() + " extraneous gizmo nodes");

                ArrayList <Item>parents = xml.getItems(top, "parent");
                for (int i = 0; i < parents.size(); i++)
                {
                    Item p = parents.get(i);
                    parent theParent = new parent(0);
                    theParent.load(xml, p);

                    System.out.println("Found a parent " + theParent.sayHi());

                    for (int j = 0; j < theParent.kids(); j++)
                    {
                        System.out.println(theParent.getChild(j).sayHi());
                    }
                }
            }

            //pkg.removeRecord(root2);
        }
        catch (Exception e) {
            System.err.println("Oh well");
            e.printStackTrace();
        }

        // save this junk
        if (pkg==null){
            pkg = new FilePackage();
        }

        try {
			String filepath = System.getProperty("user.home") + java.io.File.separator + "xmltest";
            pkg.savePackage(filepath);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
