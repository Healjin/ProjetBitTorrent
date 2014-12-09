import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ClientBitTorrent {

	public static void main(String[] args) {

		Metafile testMetafile = new Metafile("../Documents/test2.torrent");

		System.out.println(testMetafile.getAnnounce());
		Torrent testTorrent = new Torrent(testMetafile);
		Map<String, ?> responseTracker = testTorrent.request();
		System.out.println(responseTracker);
		Peers peers = new Peers(responseTracker);

		ArrayList<String> peersIP = peers.getPeersIP();
		ArrayList<Integer> peersPort = peers.getPeersPort();

		System.out.println(peersIP);
		System.out.println(peersPort);

		// Création de la trame handshake
		byte[] handshake = new byte[68];

		final String HANDSHAKE = ((char) 19) + "BitTorrent Protocol";
		System.arraycopy(HANDSHAKE.getBytes(), 0, handshake, 0, 20);
		System.arraycopy(testTorrent.getInfoHash().getBytes(), 0, handshake, 28, 20);
		System.arraycopy(testTorrent.getPeerID().getBytes(), 0, handshake, 48, 20);
		
		// Attributs de la requête handshake
		Socket mSocket;
		InputStream mIn;
		OutputStream mOut;
		DataInputStream mDataIn;
		DataOutputStream mDataOut;

		int nbPeer = 0;
		for (nbPeer = 0; nbPeer < peersIP.size(); nbPeer++) {

			try {
				System.out.println("Connection to " + peersIP.get(nbPeer) + ":" + peersPort.get(nbPeer));
				mSocket = new Socket();
				mSocket.connect(new InetSocketAddress(peersIP.get(nbPeer), peersPort.get(nbPeer)), 2000);
				System.out.println("Connected to peer");
				mOut = mSocket.getOutputStream();
				mIn = mSocket.getInputStream();
				mDataIn = new DataInputStream(mIn);
				mDataOut = new DataOutputStream(mOut);

				// Send handshake
				mSocket.setSoTimeout(3000);
				System.out.println("sending handshake");
				mDataOut.write(handshake);
				mDataOut.flush();

				// Wait response
				byte[] response = new byte[68];
				System.out.println("Waiting response");
				mDataIn.readFully(response, 0, 68);
				// mDataIn.read(response,0,1);
				System.out.println("Response received");
				System.out.println(new String(response));

				byte[] responseInfoHash = Arrays.copyOfRange(response, 28, 48);

				System.out.println(Arrays.equals(testTorrent.getInfoHash().getBytes(), responseInfoHash));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}