import java.util.Map;

public class ClientBitTorrent {

	public static void main(String[] args) {

		Metafile testMetafile = new Metafile("../Documents/test3.torrent");

		System.out.println(testMetafile.getAnnounce());
		Torrent testTorrent = new Torrent(testMetafile);
		Map<String, ?> responseTracker = testTorrent.request();
		System.out.println(responseTracker);
		Peers peers = new Peers(responseTracker);
		System.out.println(peers.getPeersIP());
		System.out.println(peers.getPeersPort());
	}
}