public class Peer {

	String ip;
	int port;
	
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
}
