package de.ur.pdits.cryptchat.test;

import de.ur.pdits.cryptchat.Main;

public class LaunchClient {

	public static void main(String[] args) {
		String[] params = { "-client", "localhost", "1337" };
		Main.main(params);
	}
}
