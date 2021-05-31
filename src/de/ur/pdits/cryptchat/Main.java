package de.ur.pdits.cryptchat;

import java.io.File;
import java.util.HashSet;

import de.ur.pdits.cryptchat.network.ChatClient;
import de.ur.pdits.cryptchat.network.ChatServer;

public class Main {

	// run args
	private final static String PARAM_HELP = "-HELP";
	private final static String PARAM_SERVER = "-SERVER";
	private final static String PARAM_CLIENT = "-CLIENT";

	private final static String PARAM_AUTHKEY = "-KEY=";
	private final static String PARAM_AUTHCERT = "-CERT=";

	public static void main(String[] args) {

		int param_port = -1;
		String param_host = "";
		String param_authkey = "";
		String param_authcert = "";

		HashSet<String> params = new HashSet<String>();


		for (int i = 0; i < args.length; i++) {
			params.add(args[i].toUpperCase());

			//Um Die Paramter vom Server mitzunehmen und überschreiben der Werte
			if (args[i].equalsIgnoreCase(PARAM_SERVER)) {
				param_port = Integer.parseInt(args[i + 1]);
			}
			//Um Die Paramter von Client mitzunehmen
			else if (args[i].equalsIgnoreCase(PARAM_CLIENT)) {
				param_host = args[i + 1];
				param_port = Integer.parseInt(args[i + 2]);
			}
			//Split nach = . Nimmt das hintere Element, was dann der Pfad zum Key ist.
			else if (args[i].toUpperCase().startsWith(PARAM_AUTHKEY)) {
				param_authkey = args[i].split("=")[1];
			}

			//Split nach = . Nimm auch das hintere Element, was dann der Pfad zum
			else if (args[i].toUpperCase().startsWith(PARAM_AUTHCERT)) {
				param_authcert = args[i].split("=")[1];
			}
		}
		//Hilfe ausprinten
		if (params.contains(PARAM_HELP)) {
			printHelp();

		//Server starten
		} else if (params.contains(PARAM_SERVER)) {
			new ChatServer(param_port, (param_authkey.isEmpty() ? null : new File(param_authkey)),
					(param_authcert.isEmpty() ? null : new File(param_authcert)));

			//Client starten
		} else if (params.contains(PARAM_CLIENT)) {
			new ChatClient(param_host, param_port, (param_authkey.isEmpty() ? null : new File(param_authkey)),
					(param_authcert.isEmpty() ? null : new File(param_authcert)));
		} else {
			printHelp();
		}
	}

	private static void printHelp() {
		System.out.println("You can run the program using the following arguments:");
		System.out.println("");
		System.out.println(PARAM_HELP);
		System.out.println("\t" + "print the help site");
		System.out.println("");
		System.out.println(PARAM_SERVER + " (port)");
		System.out.println("\t" + "Start a Server running on (port)");
		System.out.println(PARAM_CLIENT + " (host) (port)");
		System.out.println("\t" + "connect to a server running on (host):(port)");
		System.out.println("");
		System.out.println(PARAM_AUTHKEY + "XXX");
		System.out.println("\t Load authentication private key from path XXX");
		System.out.println(PARAM_AUTHCERT + "YYY");
		System.out.println("\t Load partner's authentication cert from path YYY");
		System.out.println("");
		System.out.println("\t (authentication file parameters are optional until authentication is implemented)");
	}
}
