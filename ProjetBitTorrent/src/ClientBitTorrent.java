import java.util.Map;
import java.math.BigInteger;

public class ClientBitTorrent {
	
	final static String peerID = "92890643890438943890";
	final static int socketPort = 9002;

	public static void main(String[] args) {

		Metafile metafile = new Metafile("../../Documents/test3.torrent");
		Torrent torrent = new Torrent(metafile, socketPort, peerID);
		PeersManager pm = new PeersManager(metafile, torrent.getInfoHash(), peerID);
		
		Map<String, ?> responseTracker = torrent.request();
		
		if (responseTracker == null) {
			System.out.println("Can't request the tracker");
			return;
		}
		
		int interval = ((BigInteger) responseTracker.get("interval")).intValue();
		int timeBetweenRequest = 30; // seconds
		
		while (true) {
			
			responseTracker = torrent.request();
			Peers peers = new Peers(responseTracker);
			System.out.println("Request to the tracker : " + peers.getPeers().size() + " peers availables");
			pm.setPeers(peers);
			pm.update();
			
			try {
				Thread.sleep(timeBetweenRequest * 1000);
			} catch (InterruptedException e) {
				System.err.println("Error while trying to sleep");
			}
			
		}
	}
	
}