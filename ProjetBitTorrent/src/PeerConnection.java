import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class PeerConnection {
	int[] piecesDownloaded; // 0 = non download, 1 = en cours , 2 = fini
	byte[] infoHash;
	String peerID;
	byte[] pieces;
	Peer peer;

	Socket socket;

	public PeerConnection(Peer peer, byte[] pieces, int[] piecesDownloaded, byte[] infoHash, String peerID) {
		this.peer = peer;
		this.piecesDownloaded = piecesDownloaded;
		this.pieces = pieces;
		this.infoHash = infoHash;
		this.peerID = peerID;
		

		try {
			System.out.println("Connection to " + peer.getIP() + ":" + peer.getPort());
			socket = new Socket();
			socket.connect(new InetSocketAddress(peer.getIP(), peer.getPort()), 5000);
			System.out.println("Connected to peer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handshake() {

		// Création de la trame handshake
		byte[] handshake = new byte[68];

		final String HANDSHAKE = ((char) 19) + "BitTorrent protocol";
		System.arraycopy(HANDSHAKE.getBytes(), 0, handshake, 0, 20);
		System.arraycopy(infoHash, 0, handshake, 28, 20);
		System.arraycopy(peerID.getBytes(), 0, handshake, 48, 20);

		// Attributs de la requête handshake
		InputStream mIn;
		OutputStream mOut;
		DataInputStream mDataIn;
		DataOutputStream mDataOut;

		try {
			mOut = socket.getOutputStream();
			mIn = socket.getInputStream();
			mDataIn = new DataInputStream(mIn);
			mDataOut = new DataOutputStream(mOut);

			// Send handshake
			socket.setSoTimeout(5000);
			System.out.println("sending handshake");
			mDataOut.write(handshake);
			mDataOut.flush();

			// Wait response
			byte[] response = new byte[68];
			System.out.println("Waiting response");
			mDataIn.readFully(response);
			System.out.println("Response received");
			System.out.println(new String(response));

			byte[] responseInfoHash = Arrays.copyOfRange(response, 28, 48);

			System.out.println(Arrays.equals(infoHash, responseInfoHash));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
