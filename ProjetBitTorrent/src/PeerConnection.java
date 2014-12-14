import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class PeerConnection {
	final int TIME_OUT_CONNECTION = 5000;
	final int TIME_OUT_HANDSHAKE = 5000;

	int[] piecesDownloaded; // 0 = non downloaded, 1 = in progress , 2 = download finished
	byte[] infoHash;
	String peerID;
	byte[] pieces;
	Peer peer;

	Socket socket;

	public PeerConnection(Peer peer, byte[] pieces, int[] piecesDownloaded, byte[] infoHash, String peerID) throws IOException {
		this.peer = peer;
		this.piecesDownloaded = piecesDownloaded;
		this.pieces = pieces;
		this.infoHash = infoHash;
		this.peerID = peerID;

		System.out.print("=========================================================" + "\n");
		System.out.println("Connection to " + peer.toString());
		System.out.flush();
		socket = new Socket();
		socket.connect(new InetSocketAddress(peer.getIP(), peer.getPort()), TIME_OUT_CONNECTION);
		System.out.println("Connected to peer " + peer.toString());
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
			System.out.println(response.length);

			byte[] responseInfoHash = Arrays.copyOfRange(response, 28, 48);
			byte sizeProtocolName = response[0];

			// Make sure that is the bittorrent protocol
			if ((sizeProtocolName == 19 ) && new String(response).toLowerCase().contains("bittorrent protocol")) {
				// If the hash send is the same as the hash received
				if (Arrays.equals(infoHash, responseInfoHash)) {
					// Extract peer ID and store it
					String peerID = new String(Arrays.copyOfRange(response, 48, 68));
					peer.setPeerID(peerID);
					return true; // Handshake is correct.
				}
			}
			return false; // Handshake is incorrect
		} catch (EOFException e) {
			System.out.print("Data from " + peer.toString() + " are corrupted." + "\n");
			return false;
		} catch (IOException e) {
			System.out.print("Error while tryin to read data input stream from " + peer.toString() + "\n");
			return false;
		}
	}

}
