package de.ur.pdits.cryptchat.security;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import de.ur.pdits.cryptchat.network.Connection;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import static com.sun.org.apache.bcel.internal.classfile.Utility.toHexString;
import static de.ur.pdits.cryptchat.network.ChatServer.generateSecretKey;

public class KeyExchange {


	public static SecretKey executeClientSide(Connection connection) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		// TODO: Execute diffie hellman key exchange on connection
		//This one will generate the Big Prime numbers and share them and his public key to the ServerSide.
		System.out.println("Client: Generate DH keypair ...");
		KeyPairGenerator ClientKeyPairGen = KeyPairGenerator.getInstance("DH");
		ClientKeyPairGen.initialize(2048);
		KeyPair generaredKeyPair = ClientKeyPairGen.generateKeyPair();
		System.out.println(generaredKeyPair);

		// Client creates and initializes her DH KeyAgreement object
		System.out.println("Client: Initialization ...");
		KeyAgreement ClientKeyAgreement = KeyAgreement.getInstance("DH");
		ClientKeyAgreement.init(generaredKeyPair.getPrivate());

		// Alice encodes her public key, and sends it over to Bob.
		byte[] ClientPublicKey = generaredKeyPair.getPublic().getEncoded();
		System.out.println(ClientPublicKey);
		connection.send(ClientPublicKey);

		/*
		 * Client uses Servers's public key for the first (and only) phase
		 * of her version of the DH
		 * protocol.
		 * Before Client can do so, he has to instantiate a DH public key
		 * from Server's encoded key material.
		 */
		byte[] ServerPubKeyEnc = connection.receive();
		KeyFactory ClientKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(ServerPubKeyEnc);
		PublicKey ServerPublicKey = ClientKeyFac.generatePublic(x509KeySpec);
		System.out.println("ALICE: Execute PHASE1 ...");
		ClientKeyAgreement.doPhase(ServerPublicKey, true);

		byte[] ClientSharedSecret = ClientKeyAgreement.generateSecret();
		//int aliceLen = aliceSharedSecret.length;
		System.out.println("Client secret: " +
				toHexString(ClientSharedSecret));

		//Now we have an shared key, lets generate an Secret Key out of it. We do so again with an empty IV.
		byte[] iv = new byte[12];
		String s = new String(ClientSharedSecret, StandardCharsets.UTF_8);
		SecretKey secretKey = generateSecretKey(s, iv);
		System.out.println("Diffie Hellman finished");
		return secretKey;
	}

	public static SecretKey executeServerSide(Connection connection) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
		// TODO: Execute diffie hellman key exchange on connection
		//This Key consits of the Pubkey and the two big Primes
		byte[] keySendedByClient = connection.receive();
		KeyFactory ServerKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keySendedByClient);
		PublicKey ClientPublicKey = ServerKeyFac.generatePublic(x509KeySpec);
		DHParameterSpec sharedParamsForDFH = ((DHPublicKey)ClientPublicKey).getParams();

		// Server creates his own DH key pair
		System.out.println("Server: Generate DH keypair ...");
		KeyPairGenerator ServerKeyPairGen = KeyPairGenerator.getInstance("DH");
		ServerKeyPairGen.initialize(sharedParamsForDFH);
		KeyPair ServerKeyPair = ServerKeyPairGen.generateKeyPair();

		// Server creates and initializes his DH KeyAgreement object
		System.out.println("Server: Initialization ...");
		KeyAgreement serverKeyAgree = KeyAgreement.getInstance("DH");
		serverKeyAgree.init(ServerKeyPair.getPrivate());

		// Server encodes his public key, and sends it over to Alice.
		byte[] ServerPublicKeyEnc = ServerKeyPair.getPublic().getEncoded();
		connection.send(ServerPublicKeyEnc);

		/*
		 * Server uses Bob's public key for the first (and only) phase
		 * of his version of the DH
		 * protocol.
		 */
		System.out.println("Server: Execute PHASE1 ...");
		//Erstellung des geteilten Geheimnis
		serverKeyAgree.doPhase(ClientPublicKey, true);
		byte[] serversharedSecret = serverKeyAgree.generateSecret();
		System.out.println("Server secret: " +
				toHexString(serversharedSecret));

		//Now we have an shared key, lets generate an Secret Key out of it. We do so again with an empty IV.
		byte[] iv = new byte[12];
		String s = new String(serversharedSecret, StandardCharsets.UTF_8);
		SecretKey secretKey = generateSecretKey(s, iv);
		System.out.println("Diffie Hellman finished");
		return secretKey;
	}

}
