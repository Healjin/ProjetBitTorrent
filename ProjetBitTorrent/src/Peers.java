/*-------------------------------------------------------------------------
	FILE		: 	Peers.java
	DESCRIPTION	:	This class is intended to extract IP and port of 
					peers from the tracker response. It also allows to get 
					the time between each request to the tracker, the 
					number of seeders and the number of leechers.
					All peers are stored in an ArrayList of peer.
					Each values are accessible by getters.
	AUTHORS		:	Magnin Antoine, Da Silva Andrade David
-------------------------------------------------------------------------*/
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

	/*------------------------------------------------------------------------
		DESCRIPTION	: 	This constructor is intended to extract IP and port of 
						peers from the tracker response. It also allows to get 
						the time between each request to the tracker, the 
						number of seeders and the number of leechers.
						All peers are stored in an ArrayList of peer.
		PARAMS		:	(Map<String, ?>) responseTracker
		RETURN		:	None
	------------------------------------------------------------------------*/
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
				// IP and port are stocked with 6 bytes, the first 4 are the IP and the 2 next are the port.
				System.arraycopy(tmpPeers, i, tmpIP, 0, 4);
				System.arraycopy(tmpPeers, 4 + i, tmpPort, 0, 2);

				ip = new String((int) (tmpIP[0] & 0xFF) + "." + (int) (tmpIP[1] & 0xFF) + "." + (int) (tmpIP[2] & 0xFF) + "."
						+ (int) (tmpIP[3] & 0xFF));
				port = ((tmpPort[0] & 0xFF) << 8) | (tmpPort[1] & 0xFF);
			
				peers.add(new Peer(ip, port));
			}
		}
	}
	
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Getters to access peers parameters
	-------------------------------------------------------------------------*/
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
