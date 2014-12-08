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
	ArrayList<String> peersIP;
	ArrayList<Integer> peersPort;

	Map<String, ?> responseTracker;

	public Peers(Map<String, ?> responseTracker) {
		this.responseTracker = responseTracker;

		peersIP = new ArrayList<String>();
		peersPort = new ArrayList<Integer>();

		if (responseTracker.get("interval") != null)
			interval = ((BigInteger) responseTracker.get("interval")).intValue();
		if (responseTracker.get("complete") != null)
			seeders = ((BigInteger) responseTracker.get("complete")).intValue();
		if (responseTracker.get("incomplete") != null)
			leechers = ((BigInteger) responseTracker.get("incomplete")).intValue();

		if (responseTracker.get("peers") != null) {
			byte[] peers = (byte[]) responseTracker.get("peers");
			// Temporary array to extract IP and port from each peer
			byte[] tmpIP = new byte[4];
			byte[] tmpPort = new byte[2];
			for (int i = 0; i < peers.length; i += 6) {
				System.arraycopy(peers, i, tmpIP, 0, 4);
				System.arraycopy(peers, 4 + i, tmpPort, 0, 2);

				peersIP.add((int) (tmpIP[0] & 0xFF) + "." + (int) (tmpIP[1] & 0xFF) + "." + (int) (tmpIP[2] & 0xFF) + "." + (int) (tmpIP[3] & 0xFF));
				peersPort.add(((tmpPort[0] & 0xFF) << 8) | (tmpPort[1] & 0xFF));
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

	public ArrayList<String> getPeersIP() {
		return peersIP;
	}

	public ArrayList<Integer> getPeersPort() {
		return peersPort;
	} 
}
