/*-------------------------------------------------------------------------
	FILE		: 	ClientBitTorrent.java
	DESCRIPTION	:	Implementation of a BitTorrent client as a badleecher.
	AUTHORS		:	Magnin Antoine, Da Silva Andrade David
-------------------------------------------------------------------------*/
import java.util.Map;

public class ClientBitTorrent {

	final static int MAX_PEERS_CONNECTIONS = 5;
	final static String peerID = "92890643890438943890";
	final static int socketPort = 9002;
	static String nameFile;

	public static void main(String[] args) {
		
		if(args.length != 1)
		{
			System.out.println("Usage : java ClientBitTorrent <path_to_torrent_file>");
			// Quitte l'application
			System.exit(1);
		}
		nameFile = args[0];	
		
		// Extract all data from the torrent file
		Metafile metafile = new Metafile(nameFile);
		
		// Implementation of THP protocol
		Torrent torrent = new Torrent(metafile, socketPort, peerID);
		
		// Create a manager
		PeersManager pm = new PeersManager(metafile, torrent.getInfoHash(), peerID, MAX_PEERS_CONNECTIONS);
		
		int timeBetweenRequest = 30; // seconds
		Map<String, ?> responseTracker;
		
		while (true) {
			
			// Send request to tracker
			responseTracker = torrent.request();
			
			// If no response
			if (responseTracker == null) {
				System.out.println("Can't request the tracker");
				return;
			}
			
			// Extract all peers from the tracker response
			Peers peers = new Peers(responseTracker);
			
			// Get time between each request to the tracker
			//timeBetweenRequest = peers.getInterval();
			
			System.out.println("Request to the tracker : " + peers.getPeers().size() + " peers availables");
			
			// Update all peers with the new ones
			pm.updatePeers(peers);
			
			// Wait between each request to tracker
			try {
				Thread.sleep(timeBetweenRequest * 1000);
			} catch (InterruptedException e) {
				System.err.println("Error while trying to sleep");
			}
			
		}
	}
	
}