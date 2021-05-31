package de.ur.pdits.cryptchat.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import de.ur.pdits.cryptchat.network.Connection;

public class Authentication {

	public static X509Certificate loadCert(File certFile) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			FileInputStream finStream;
			finStream = new FileInputStream(certFile);
			X509Certificate cert = (X509Certificate) cf.generateCertificate(finStream);
			finStream.close();
			return cert;
		} catch (Exception e) {
			System.out.println("Failed to load certificate from path " + certFile.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	public static PrivateKey loadPrivateKey(File keyFile) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			KeySpec ks = new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(keyFile.getAbsolutePath())));
			PrivateKey key = kf.generatePrivate(ks);
			return key;
		} catch (Exception e) {
			System.out.println("Failed to load private key from path " + keyFile.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}




	public static byte[] signChallenge(byte[] challenge, PrivateKey signKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		// TODO: Sign the challenge with the provided private key Done
		Signature sig = Signature.getInstance("SHA256WithRSA");
		sig.initSign(signKey);
		sig.update(challenge);
		byte[] signatureBytes = sig.sign();
		return signatureBytes;
	}

	public static boolean signatureValid(byte[] challenge, byte[] signature, X509Certificate cert) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		// TODO: verify signature (test if decrypted response matches the unencrypted challenge) Done
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		publicSignature.initVerify(cert);
		publicSignature.update(challenge);
		return publicSignature.verify(signature);
	}

	/**
	 * 
	 * @param authKeyFile
	 *            File containing the own private key to sign server's
	 *            challenges with
	 * @param partnerAuthCertFile
	 *            File containing the server's certificate to validate his
	 *            signatures with
	 * @return True if authentication was successful, false else
	 */

	//Client = User 1
	public static boolean executeClient(Connection connection, File authKeyFile, File partnerAuthCertFile) throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		// TODO: Verify server's authenticity
		// 0) load client's private key and server's cert
		//Server Cert
		X509Certificate ServerCer = loadCert(partnerAuthCertFile);
		//Load Private Key
		PrivateKey clientPrivKey = loadPrivateKey(authKeyFile);

		// 1) Send client's challenge
		byte[] bytes = new byte[128];
		SecureRandom.getInstanceStrong().nextBytes(bytes);
		connection.send(bytes);

		// 2) receive signed challenge
		byte[] signedChallenge = connection.receive();

		// 3) receive server's challenge
		byte[] serverChallenge = connection.receive();

		// 4) respond signed challenge
		byte[] ourRespondToChallenge =  signChallenge(serverChallenge,clientPrivKey);
		connection.send(ourRespondToChallenge);

		// 5) return true if server's signature is valid
		boolean OurChallenge =signatureValid(bytes,signedChallenge,ServerCer);
		if(OurChallenge) {
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * 
	 * @param authKeyFile
	 *            File containing the own private key to sign client's
	 *            challenges with
	 * @param partnerAuthCertFile
	 *            File containing the client's certificate to validate his
	 *            signature with
	 * @return True if authentication was successful, false else
	 */
	public static boolean executeServer(Connection connection, File authKeyFile, File partnerAuthCertFile) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		// TODO: Verify client's authenticity
		// 0) load server's private key and client's cert
		//Server Cert
		X509Certificate clientZertifikat = loadCert(partnerAuthCertFile);
		//Load Private Key
		PrivateKey clientPrivKey = loadPrivateKey(authKeyFile);

		// 1) receive client's challenge
		byte[] clientChallenge = connection.receive();

		// 2) respond signed challenge
		byte[] ourRespondToChallenge =  signChallenge(clientChallenge,clientPrivKey);
		connection.send(ourRespondToChallenge);

		// 3) send server's challenge
		byte[] bytes = new byte[128];
		SecureRandom.getInstanceStrong().nextBytes(bytes);
		connection.send(bytes);

		// 4) receive signed challenge
		byte[] signedChallengeFromClient = connection.receive();

		// 5) return true if client's signature is valid
		boolean OurChallenge =signatureValid(bytes,signedChallengeFromClient,clientZertifikat);
		if(OurChallenge) {
			return true;
		}
		else{
			return false;
		}
	}

}
