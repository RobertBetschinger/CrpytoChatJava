package de.ur.pdits.cryptchat.test;

import de.ur.pdits.cryptchat.Main;

public class LaunchClientWithAuthentication {

	public static void main(String[] args) {
		String[] params = { "-client", "localhost", "1337", "-key=C:\\Users\\rober\\Desktop\\user1.key",
				"-cert=C:\\Users\\rober\\Desktop\\user2.crt" };
		Main.main(params);
	}
}
