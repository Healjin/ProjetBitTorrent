import java.util.ArrayList;

public class PeerManager {

	Peers peers;
	byte[] pieces;
	int[] piecesDownloaded; // 0 = non download, 1 = en cours , 2 = fini
	byte[] infoHash;
	String peerID;

	ArrayList<PeerConnection> peerConnections;

	public PeerManager(Peers peers, byte[] pieces, byte[] infoHash, String peerID) {

		this.peers = peers;
		this.pieces = pieces;
		this.infoHash = infoHash;
		this.peerID = peerID;

		piecesDownloaded = new int[pieces.length / 20];
		for (int i = 0; i < piecesDownloaded.length; i++) {
			piecesDownloaded[i] = 0;
		}
		peerConnections = new ArrayList<PeerConnection>();

	}

	public void startDownload() {
		ArrayList<Peer> listPeers = peers.getPeers();

		for (Peer peer : listPeers) {
			try {
				PeerConnection peerConnection = new PeerConnection(peer, pieces, piecesDownloaded, infoHash, peerID);
				peerConnection.handshake();
				peerConnections.add(peerConnection);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
