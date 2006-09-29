package org.cl.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class DeployTomcat {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length == 3) {
			if (args[2].startsWith("http")) {
				try {
					newURLCon(args[2], args[0], args[1]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.out
					.println("Error: Please use syntax DeployTomcat user password url");
		}

	}

	public static void newURLCon(String con, String username, String password)
			throws IOException {
		// Install the custom authenticator
		Authenticator.setDefault(new MyAuthenticator(username, password));

		con = con.replaceAll(" ", "+");

		Date sDate = new Date();
		URL url = new URL(con);
		URLConnection urlConnection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream()));

		String inputLine;
		String result = "";
		while ((inputLine = in.readLine()) != null) {
			result = result + inputLine;
			System.out.println(inputLine);
		}

		in.close();
		Date eDate = new Date();
		long ms = eDate.getTime() - sDate.getTime();

		// System.out.println("Called: " + con + ".In " + ms + "milliseconds");
		// System.out.println("Start Time:" + sDate + "End Date:" + eDate);
	}

}