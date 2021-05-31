package de.ur.pdits.cryptchat.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import de.ur.pdits.cryptchat.security.Encryption;

public class Connection {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private Encryption encryption = null;

	/**
	 * Initiates the connection. After initiating, both sides can send bytes[]
	 * through send(byte[]) and receive bytes[] via receive() (can be used to
	 * execute authentication prototcols etc). After succesful initialization,
	 * the chat can be initiated via startChat().
	 * 
	 * @param socket
	 */
	public Connection(Socket socket) {

		try {
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public void startChat() {

		try {

			/* Start Threads */
			Thread consoleInputThread = new Thread(new ConsoleInput());
			Thread socketInputThread = new Thread(new SocketInput());

			socketInputThread.start();
			consoleInputThread.start();

		} catch (Exception e) {
			System.out.println("Failed to initiate connection:");
			e.printStackTrace();
		}

	}

	public void setEncryption(Encryption encryption) {
		this.encryption = encryption;
	}

	private class ConsoleInput implements Runnable {
		@Override
		public void run() {
			Scanner scanIn = new Scanner(System.in);
			while (true) {
				try {
					String line = scanIn.nextLine();
					byte[] output = line.getBytes();
					if (encryption != null)

						output = encryption.encryptSymmetrically(output);

					send(output);
				} catch (Exception e) {
					System.out.println("An error occured, terminating.");
					scanIn.close();
					System.exit(0);
				}

			}

		}

	}

	private class SocketInput implements Runnable {
		@Override
		public void run() {
			while (true) {
				byte[] input = receive();
				if (input == null) {
					System.out.println("Connection interrupted, shutting down.");
					System.exit(0);
				}
				try {
					if (encryption != null)
						input = encryption.decryptSymmetrically(input);
					System.out.println("> " + new String(input));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void send(byte[] arr) {
		try {
			out.writeInt(arr.length);
			out.write(arr);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] receive() {
		try {
			byte[] b = new byte[in.readInt()];
			in.read(b);
			return b;
		} catch (Exception e) {
			return null;
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void close() {
		try {
			in.close();
			out.close();
			System.exit(0);
		} catch (IOException e) {
		}
	}

}
