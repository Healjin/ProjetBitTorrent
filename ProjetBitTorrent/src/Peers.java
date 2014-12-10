import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

public class Peers {

	/*----------------
		ATTRIBUTES
	----------------*/
	Integer interval;
	Integer seeders;
	Integer leechers;
	ArrayList<Peer> peers;
	ArrayList<String> peersIP;
	ArrayList<Integer> peersPort;

	Map<String, ?> responseTracker;

	public Peers(Map<String, ?> responseTracker) {
		this.responseTracker = responseTracker;

		peers = new ArrayList<Peer>();

		if (responseTracker.get("interval") != null)
			interval = ((BigInteger) responseTracker.get("interval")).intValue();
		if (responseTracker.get("complete") != null)
			seeders = ((BigInteger) responseTracker.get("complete")).intValue();
		if (responseTracker.get("incomplete") != null)
			leechers = ((BigInteger) responseTracker.get("incomplete")).intValue();

		if (responseTracker.get("peers") != null) {
			byte[] tmpPeers = (byte[]) responseTracker.get("peers");
			// Temporary array to extract IP and port from each peer
			byte[] tmpIP = new byte[4];
			byte[] tmpPort = new byte[2];
			String ip;
			int port;
			for (int i = 0; i < tmpPeers.length; i += 6) {
				System.arraycopy(tmpPeers, i, tmpIP, 0, 4);
				System.arraycopy(tmpPeers, 4 + i, tmpPort, 0, 2);

				ip = new String((int) (tmpIP[0] & 0xFF) + "." + (int) (tmpIP[1] & 0xFF) + "." + (int) (tmpIP[2] & 0xFF) + "."
						+ (int) (tmpIP[3] & 0xFF));
				port = ((tmpPort[0] & 0xFF) << 8) | (tmpPort[1] & 0xFF);
				peers.add(new Peer(ip, port));
			}
		}
	}

	public Integer getInterval() {
		return interval;
	}

	public Integer getSeeders() {
		return seeders;
	}

	public Integer getLeechers() {
		return leechers;
	}

	public ArrayList<Peer> getPeers() {
		return peers;
	}
}
