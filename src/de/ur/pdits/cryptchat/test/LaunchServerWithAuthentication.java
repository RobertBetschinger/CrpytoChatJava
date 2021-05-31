package de.ur.pdits.cryptchat.test;

import de.ur.pdits.cryptchat.Main;

public class LaunchServerWithAuthentication {

	public static void main(String[] args) {
		String[] params = { "-server", "1337", "-key=C:\\Users\\rober\\Desktop\\user2.key",
				"-cert=C:\\Users\\rober\\Desktop\\user1.crt" };
		Main.main(params);
	}

}
