import java.util.Map;

public class ClientBitTorrent {
	
	final static String peerID = "01234567890123456789";

	public static void main(String[] args) {

		Metafile metafile = new Metafile("../Documents/test1.torrent");

		System.out.println(metafile.getAnnounce());
		Torrent torrent = new Torrent(metafile, 9002, peerID);
		Map<String, ?> responseTracker = torrent.request();
		System.out.println(responseTracker);
		Peers peers = new Peers(responseTracker);
		PeerManager pm = new PeerManager(peers, metafile.getPieces(), torrent.getInfoHash(), peerID);
		pm.startDownload();
		
		
	}
}