import java.util.Map;

public class ClientBitTorrent {
	
	final static String peerID = "92890643890438943890";
	final static int socketPort = 9002;

	public static void main(String[] args) {

		Metafile metafile = new Metafile("../../Documents/test1.torrent");

		Torrent torrent = new Torrent(metafile, socketPort, peerID);
		Map<String, ?> responseTracker = torrent.request();
		System.out.println(responseTracker);
		Peers peers = new Peers(responseTracker);
		System.out.println("Number of peers : " + peers.getPeers().size());
		PeersManager pm = new PeersManager(peers, metafile, torrent.getInfoHash(), peerID);
		pm.startDownload();
		
		
	}
}