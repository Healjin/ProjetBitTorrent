public class Peer {

	String ip;
	int port;
	String peerID;
	
	public Peer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIP(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}

	@Override
	public String toString(){
		return ip + ":" + port;
	}
	
	public void setPeerID(String peerID){
		this.peerID = peerID;
	}
	
	public String getPeerID(){
		return peerID;
	}
}
