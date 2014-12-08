import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

		try {
			InetAddress addr = InetAddress.getByName(peersIP.get(0));
			Socket clientSocket = new Socket(addr, peersPort.get(0));
			byte[] nameLength = new byte[] { 19 };
			String protocolName = "BitTorrent protocol";
			byte[] reserved = new byte[8];
			byte[] infoHash = testTorrent.getInfoHash().getBytes();
			byte[] peerID = testTorrent.getPeerID().getBytes();

			String handshake = new String(nameLength) + protocolName + new String(reserved) + new String(infoHash) + new String(peerID);
			
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
		System.out.println(peers.getPeersPort());
	}
}