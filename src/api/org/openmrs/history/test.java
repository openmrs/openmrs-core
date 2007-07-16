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
    String something;

    public void setSomething(String s) {something=s;}
    public String getSomething() {return something;}

    public String toString()
    {
        return ("OriginalHistory! says '" + getName() + " is " + getAge() 
                + " and " + getSomething() + "'");
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
        history.setSomething("this is a test");

        System.out.println("Simple history created is " + history);
    }
}
