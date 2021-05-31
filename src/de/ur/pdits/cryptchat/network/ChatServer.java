package de.ur.pdits.cryptchat.network;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import de.ur.pdits.cryptchat.security.Authentication;
import de.ur.pdits.cryptchat.security.Encryption;
import de.ur.pdits.cryptchat.security.KeyExchange;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static de.ur.pdits.cryptchat.security.KeyExchange.executeServerSide;

public class ChatServer {

	ServerSocket serverSocket;

	/**
	 * 
	 * @param port
	 *            The port to listen for client's conenction
	 * @param authKeyFile
	 *            Points to the authentication
	 * @param partnerAuthCertFile
	 */
	//Methode zum erstellen des Servers
	public ChatServer(int port, File authKeyFile, File partnerAuthCertFile) {

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Failed to listen on port " + port + ".");
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("Waiting for incoming connection...");

		try {

			// 1/3) Establish network connection to send and receive byte arrays
			Connection connection = new Connection(serverSocket.accept());

			System.out.println("Connection established from " + connection.getSocket().getInetAddress() + ":"
					+ connection.getSocket().getPort());

			// 2/3) Execute security methods to ensure confidential
			// communication
			//Anscheinend muss sich der Client auch autentifizieren, sonst wird die Connection beendet
			if (Authentication.executeServer(connection, authKeyFile, partnerAuthCertFile)) {
		// Hier füge ich gerade eine Exception ein.
				initEncryption(connection);

				// 3/3) Switch into chat mode: Send any user inputs through the
				// connection, print any received messages on the console.
				System.out.println("Security Handshake finished, starting chat.");

				connection.startChat();
			} else {
				System.out.println("Failed to confirm client's authenticity. Closing connection.");
				connection.close();
			}

		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | SignatureException e) {
			System.out.println("Failed to establish secure connection:");
			e.printStackTrace();
		}
	}

	public static SecretKey generateSecretKey(String password, byte [] iv) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), iv, 65536, 256); // AES-128
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
		return new SecretKeySpec(key, "AES");
	}


	private void initEncryption(Connection connection) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
		// TODO:
		//Hier Teilafugabe 1, welche durch DFH ja überflüssig wurde.
		// CryptChat 1.0: Initiate Cipher & hand it over to connection via
		// Connection.setEncryption()
		//byte[] iv = new byte[12];
		//String key ="12345678";
		//Generation of THE Secret Key with Method delieverd from Java Cryto Module
		//SecretKey secretKey = generateSecretKey(key, iv);
		//connection.setEncryption(new Encryption(secretKey));


		// CryptChat 2.0: Perform automatic KeyExchange using DiffieHellman to
		// ensure Perfect Forward Secrecy
		SecretKey diffieHellamnGeneratedKey = executeServerSide(connection);
		connection.setEncryption(new Encryption(diffieHellamnGeneratedKey));


	}
}
