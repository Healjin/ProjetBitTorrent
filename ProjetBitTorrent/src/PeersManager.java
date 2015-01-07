import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class PeersManager {
	
	Peers peers;
	int[] piecesDownloaded; // 0 = non downloaded, 1 = in progress , 2 = download finished
	byte[] infoHash;
	String peerID;
	Metafile metafile;
	ArrayList<PeerConnection> peerConnections;

	public PeersManager(Peers peers, Metafile metafile, byte[] infoHash, String peerID) {

		this.peers = peers;
		this.infoHash = infoHash;
		this.peerID = peerID;
		this.piecesDownloaded = new int[metafile.getPieces().length / 20];
		this.peerConnections = new ArrayList<PeerConnection>();
		this.metafile = metafile;

	}

	public void startDownload() {
		
		ArrayList<Peer> listPeers = peers.getPeers();

		// Do all handhsakes
		for (Peer peer : listPeers) {
			
			try {
				
				// Throw exception if connection drop out
				PeerConnection peerConnection = new PeerConnection(peer, metafile, piecesDownloaded, infoHash, peerID);
				
				// If the handshake worked as expected, return true.
				if (peerConnection.handshaken) {
					
					// At this point we know that peerConnection is valid because the connection worked and the handshake too
					this.peerConnections.add(peerConnection);
					System.out.println("Handshake OK");
					
					peerConnection.start();
					break;
					
				} // else, discard connection
				
			} catch (SocketTimeoutException e) {
				System.out.print("Time out connection to " + peer.toString() + "." + "\n");
			} catch (ConnectException e) {
				System.out.print("Connection to " + peer.toString() + " refused." + "\n");
			} catch (Exception e) {
				System.out.print("Unexpected error while trying to connect to " + peer.toString() + "\n");
			}
			
		}
		
	}
	
}
