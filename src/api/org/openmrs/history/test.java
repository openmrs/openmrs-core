package org.openmrs.history;

import java.util.*;

//
// very simple test of the HistoryFactory using a very basic
// bean (Original) and a simple extended History object using the
// temporary! IHistory interface

class Original
{
    private String name;
    private Integer age;

    public String getName() { return name;}
    public void setName(String s) {name = s;}

    public Integer getAge() { return age;}
    public void setAge(Integer s) {age = s;}

}

class OriginalHistory extends Original implements IHistory
{
	private Integer historyId = new Integer((int)System.currentTimeMillis());

    public Integer getHistoryId() {return historyId;}
    public void setHistoryId(Integer id) {historyId = id;}

    public String toString()
    {
        return ("OriginalHistory! says '" + getName() + " is " + getAge() 
                + " and " + getHistoryId() + "'");
    }
}

public class test
{
    public static void main(String argv[]) 
    {
        Original org = new Original();

        org.setName("fred");
        org.setAge(new Integer(10));

        // IHistory
        OriginalHistory history = (OriginalHistory)HistoryFactory.createHistory(org);

        System.out.println("Simple history created is " + history);
    }
}
