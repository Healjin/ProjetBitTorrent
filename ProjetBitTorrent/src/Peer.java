/*-------------------------------------------------------------------------
	FILE		: 	Peer.java
	DESCRIPTION	:	This class describe an object peer, which is defined by
					a name (peerID) and an address (ip + port).
	AUTHORS		:	Magnin Antoine, Da Silva Andrade David
-------------------------------------------------------------------------*/
public class Peer {

	String ip;
	int port;
	String peerID;
	
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Constructor to create a peer with the parameters sent
		PARAMS		: 	(String) ip
		 				(int) port
		RETURN		: 	None
	-------------------------------------------------------------------------*/
	public Peer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Getters to access peers parameters
	-------------------------------------------------------------------------*/
	public String getIP(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public String getPeerID(){
		return peerID;
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Override the method toString() to return the couple
						ip and port.
		PARAMS		:	None
		RETURN		:	(String) ip and port concatenated
	-------------------------------------------------------------------------*/
	@Override
	public String toString(){
		return ip + ":" + port;
	}
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Set peerID
		PARAMS		:	(String) peerID
		RETURN		:	None
	-------------------------------------------------------------------------*/
	public void setPeerID(String peerID){
		this.peerID = peerID;
	}
}
