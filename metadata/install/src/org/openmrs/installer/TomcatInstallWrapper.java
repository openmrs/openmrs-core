package org.openmrs.installer;

import java.io.IOException;

public class TomcatInstallWrapper {

	public TomcatInstallWrapper() {
		// TODO Auto-generated constructor stub
	}

	public static void runapp(String application) {

		try {
			Runtime.getRuntime().exec("cmd /c start " + application);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			String application = args[0];
			runapp(application);
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("Please execute:\n"
					+ TomcatInstallWrapper.class.getCanonicalName()
					+ " \"application\"");
		}
	}

}
