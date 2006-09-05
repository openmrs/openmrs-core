package org.openmrs.arden;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class MLMEvaluateElement {
	private LinkedList<String> thisList;
	
	public MLMEvaluateElement() {
		thisList = new LinkedList <String>();
	}
	
	public void add(String s){
		thisList.add(s);
	}
	
	public String getLast(){
		return thisList.getLast();
	}
	
//	public void removeThen() {
//		if(thisList.getLast().equals("THEN")) {
//			thisList.removeLast();
//		}
//	}
	public void printThisList(){
		System.out.println("\n This list evaluate order is  - ");
		ListIterator<String> iter = thisList.listIterator(0);
		while (iter.hasNext()){
		     System.out.println(iter.next());
		}
		System.out.println("----------------------");
	}
	
	public Iterator <String> iterator(){
		Iterator iter;
		return iter = thisList.iterator();
	}
	
}
