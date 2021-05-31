package de.ur.pdits.cryptchat.network;

import java.io.File;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import de.ur.pdits.cryptchat.security.Authentication;
import de.ur.pdits.cryptchat.security.Encryption;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import static de.ur.pdits.cryptchat.network.ChatServer.generateSecretKey;
import static de.ur.pdits.cryptchat.security.KeyExchange.executeClientSide;
import static de.ur.pdits.cryptchat.security.KeyExchange.executeServerSide;

public class ChatClient {

	//Funktion für connecten.
	public ChatClient(String serverHostname, int serverPort, File authKeyFile, File partnerAuthCertFile) {
		System.out.println("Connecting to server " + serverHostname + ":" + serverPort + " ...");

		Socket socket;
		try {
			socket = new Socket(serverHostname, serverPort);
		} catch (Exception e) {
			System.out.println("Failed to connect to " + serverHostname + ":" + serverPort + ".");
			return;
		}

		try {

			// 1/3) Establish network connection to send and receive byte arrays
			Connection connection = new Connection(socket);

			//Ausgeben der Connection
			System.out.println("Connection established to " + connection.getSocket().getInetAddress() + ":"
					+ connection.getSocket().getPort());

			// 2/3) Execute security methods to ensure confidential
			// communication. Servers muss sich zwangsweise authentifizieren, sonst keine Connection
			//Authentication ist die Klasse
			if (Authentication.executeClient(connection, authKeyFile, partnerAuthCertFile)) {
				initEncryption(connection);

				// 3/3) Switch into chat mode: Send any user inputs through the
				// connection, print any received messages on the console.
				System.out.println("Security Handshake finished, starting chat.");


				connection.startChat();

			} else {
				System.out.println("Failed to confirm server's authenticity. Closing connection.");
				connection.close();
			}

		} catch (Exception e) {
			System.out.println("Failed to establish secure connection:");
			e.printStackTrace();
		}

	}

	private void initEncryption(Connection connection) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
		// TODO:
		//Hier Teilafugabe 1, welche durch DFH ja überflüssig wurde.
		// CryptChat 1.0: Initiate Cipher & hand it over to connection via
		//Noonce should be 12 bytes
		//byte[] iv = new byte[12];
		//String key ="12345678";
		//Generation of THE Secret Key with Method delieverd from Java Cryto Module
		//SecretKey secretKey = generateSecretKey(key, iv);
		//connection.setEncryption(new Encryption(secretKey));


		// CryptChat 2.0: Perform automatic KeyExchange using DiffieHellman to
		// ensure Perfect Forward Secrecy
		SecretKey diffieHellamnGeneratedKey = executeClientSide(connection);
		connection.setEncryption(new Encryption(diffieHellamnGeneratedKey));

	}
}
