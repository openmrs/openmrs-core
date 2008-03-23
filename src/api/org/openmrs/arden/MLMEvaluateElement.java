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
