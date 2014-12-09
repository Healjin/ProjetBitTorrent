import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ClientBitTorrent {

	public static void main(String[] args) {

		Metafile testMetafile = new Metafile("../Documents/test1.torrent");

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
		
		Socket mSocket;
		InputStream mIn;
		OutputStream mOut;
		DataInputStream mDataIn;
		DataOutputStream mDataOut;

		int nbPeer = 2;
		
		try {
			System.out.println("Connection to " + peersIP.get(nbPeer) + ":" + peersPort.get(nbPeer));
			mSocket = new Socket(peersIP.get(nbPeer), peersPort.get(nbPeer));
			System.out.println("Connected to peer");
			mOut = mSocket.getOutputStream();
			mIn = mSocket.getInputStream();
			mDataIn = new DataInputStream(mIn);
			mDataOut = new DataOutputStream(mOut);

			// Send handshake
			mSocket.setSoTimeout(20000);
			System.out.println("sending handshake");
			mDataOut.write(handshake);
			mDataOut.flush();
			
			// Wait response
			byte[] response = new byte[68];
			System.out.println("Waiting response");
			mDataIn.readFully(response, 0, 68);
			//mDataIn.read(response,0,1);
			System.out.println("Response received");
			System.out.println(new String(response));

			byte[] responseInfoHash = Arrays.copyOfRange(response, 28, 48);
			
			System.out.println(Arrays.equals(testTorrent.getInfoHash().getBytes(), responseInfoHash));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		try {
			InetAddress addr = InetAddress.getByName(peersIP.get(0));
			Socket clientSocket = new Socket(addr, peersPort.get(0));
			byte[] nameLength = new byte[] { 19 };
			String protocolName = "BitTorrent protocol";
			byte[] reserved = new byte[8];
			byte[] infoHash = testTorrent.getInfoHash().getBytes();
			byte[] peerID = testTorrent.getPeerID().getBytes();

			
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.print(handshake);

			out.close();
			clientSocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ServerSocket serverSocket = new ServerSocket(peersPort.get(0));
			System.out.println("HELLO1");
			Socket so = serverSocket.accept();
			
			BufferedReader input = new BufferedReader( new InputStreamReader(so.getInputStream()));
			
			System.out.println("HELLO2");
			String responsePeer = input.readLine();
			System.out.println(responsePeer);
			input.close();
			so.close();
			serverSocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(peers.getPeersIP());
		System.out.println(peers.getPeersPort());*/
	}
}