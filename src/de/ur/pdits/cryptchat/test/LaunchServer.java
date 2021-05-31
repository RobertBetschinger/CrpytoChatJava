package de.ur.pdits.cryptchat.test;

import de.ur.pdits.cryptchat.Main;

public class LaunchServer {

	public static void main(String[] args) {
		String[] params = { "-server", "1337" };
		Main.main(params);
	}

}
