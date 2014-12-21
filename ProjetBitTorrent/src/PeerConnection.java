import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.nio.ByteBuffer;

public class PeerConnection extends Thread {
	
	final int TIME_OUT_CONNECTION = 5000;
	final int TIME_OUT_HANDSHAKE = 5000;

	int[] piecesDownloaded; // 0 = non downloaded, 1 = in progress , 2 = download finished
	byte[] infoHash;
	String peerID;
	byte[] pieces;
	Peer peer;
	Socket socket;
	Boolean handshaken = false;
	Boolean choked = false;
	Boolean interested = false;

	public PeerConnection(Peer peer, byte[] pieces, int[] piecesDownloaded, byte[] infoHash, String peerID) throws IOException {
		
		// Initialize attributes
		this.peer = peer;
		this.piecesDownloaded = piecesDownloaded;
		this.pieces = pieces;
		this.infoHash = infoHash;
		this.peerID = peerID;

		// Initlialize connection to peer
		System.out.print("=========================================================" + "\n");
		System.out.println("Connection to " + peer.toString());
		System.out.flush();
		socket = new Socket();
		socket.connect(new InetSocketAddress(peer.getIP(), peer.getPort()), TIME_OUT_CONNECTION);
		System.out.println("Connected to peer " + peer.toString());
		
		// Proceed to the handshake
		handshaken = handshake();
		
	}

	public Boolean handshake() {

		byte[] handshake = new byte[68];

		// Creation handshake frame
		final String HANDSHAKE = ((char) 19) + "BitTorrent protocol";
		System.arraycopy(HANDSHAKE.getBytes(), 0, handshake, 0, 20);
		System.arraycopy(infoHash, 0, handshake, 28, 20);
		System.arraycopy(peerID.getBytes(), 0, handshake, 48, 20);

		// Streams to interact with the socket
		InputStream mIn;
		OutputStream mOut;
		DataInputStream mDataIn;
		DataOutputStream mDataOut;

		try {
			
			// Retrieve all socket streams
			mOut = socket.getOutputStream();
			mIn = socket.getInputStream();
			mDataIn = new DataInputStream(mIn);
			mDataOut = new DataOutputStream(mOut);

			// Set time out value
			socket.setSoTimeout(TIME_OUT_HANDSHAKE);
			System.out.println("sending handshake");
			
			// Send handshake
			mDataOut.write(handshake);
			mDataOut.flush();

			// Wait response
			byte[] response = new byte[68];
			System.out.println("Waiting response");
			System.out.flush();

			// Read response from peer
			mDataIn.readFully(response);

			System.out.println("Response received");

			byte[] responseInfoHash = Arrays.copyOfRange(response, 28, 48);
			byte sizeProtocolName = response[0];

			// Make sure that is the bittorrent protocol
			if ((sizeProtocolName == 19 ) && new String(response).toLowerCase().contains("bittorrent protocol")) {
				
				// If the hash send is the same as the hash received
				if (Arrays.equals(infoHash, responseInfoHash)) {
					
					// Extract peer ID and store it
					String peerID = new String(Arrays.copyOfRange(response, 48, 68));
					peer.setPeerID(peerID);
					
					// Handshake is correct.
					return true;
					
				}
				
			}
			
			// Handshake is incorrect
			return false; 
			
		} catch (EOFException e) {
			System.out.print("Data from " + peer.toString() + " are corrupted." + "\n");
			return false;
		} catch (IOException e) {
			System.out.print("Error while tryin to read data input stream from " + peer.toString() + "\n");
			return false;
		}
		
	}
	
	public void run() {
		
		// Start listening to peer
		ReceiveMessages rm = new ReceiveMessages();
		rm.start();
		
	}
		
	public class ProcessMessage extends Thread {
		
		byte[] msg;
		
		public ProcessMessage(byte[] msg) {
			this.msg = msg;
		}
		
		public void run() {
			
			System.out.println("New message with ID : " + msg[4]);

		}
		
	}
	
	public class ReceiveMessages extends Thread {

		public void run() {
			
			try {

				ByteBuffer bb = ByteBuffer.allocate(1500);
				int msgLength = 1500;
				DataInputStream input = new DataInputStream(socket.getInputStream());
				System.out.println("Start listening to messages");
				
				while (true) {

					// Check if some data is available
					int size = input.available();
					
					// Some data is available
					if (size > 0) {
						
						// Get bytes on by one
						for (int i = 0; i < size; i++) {
							
							bb.put(input.readByte());
							
							// Get the length of the message
							if (bb.position() == 4) {
								msgLength = bb.getInt(0);
								System.out.println("Message length : " + bb.getInt(0));
							}
							
							// Avoid "keep-alive" messages
							if (msgLength == 0) {
								bb.clear();	
							}
							
							// Get and process the message
							if (bb.position() == msgLength + 4) {
								byte[] msg = new byte[msgLength + 4];
								bb.rewind();
								bb.get(msg, 0, msgLength + 4);
								bb.clear();
								ProcessMessage pm = new ProcessMessage(msg);
								pm.start();
							}
							
						}
						
					}
					
				}
				
			} catch (Exception e) {
				System.err.println("Error while reading socket");
			}

		}

	}
	
	public class SendMessage extends Thread {
		
		public void run() {
			
		}
		
	}	

}
