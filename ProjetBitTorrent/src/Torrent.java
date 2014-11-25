import java.security.MessageDigest;
import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Torrent {

	private Metafile torrent = null;
	private String infoHash = null;
	private String peerID = null;
	private Integer port = null;
	private Integer uploaded = null;
	private Integer downloaded = null;
	private Integer left = null;
	private String ip = null;
	private Integer numWant = null;
	private String event = null;
	
	public Torrent(Metafile file) {
		
		// Get the torrent file
		this.torrent = file;
		
		// Allocate memory on disk for downloaded file
		try {
			RandomAccessFile tmp = new RandomAccessFile(torrent.getName(), "rw");
			tmp.setLength(torrent.getLength());
			tmp.close();
		} catch (Exception e) {
			System.err.println("Error while creating tmp file");	
		}
		
		// Get SHA-1 infoHash
		try {
			MessageDigest md = null;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1 = md.digest("lol".getBytes());
			this.infoHash = URLEncoder.encode(sha1.toString(), "UTF-8");
		} catch (Exception e) {
			System.err.println("Error while getting SHA-1 infoHash");
		}
		
		// Set peerID
		this.peerID = "01234567890123456789";
		
		// Set port for listening
		this.port = 8000;
		
		// Set downloaded, uploaded and left values
		this.uploaded = 0;
		this.downloaded = 0;
		this.left = torrent.getLength();
		
		// Set event status
		this.event = "started";
		
	}
	
	public String Request() {
		
		try {
			
			// Initialize http connexion
			URL url = new URL(torrent.getAnnounce() +
							  "?info_hash=" + this.infoHash +
							  "&peer_id=" + this.peerID +
							  "&port=" + this.port +
							  "&uploaded=" + this.uploaded +
							  "&downloaded=" + this.downloaded +
							  "&left=" + this.left +
							  "&event=" + this.event);
			HttpURLConnection connexion = (HttpURLConnection) url.openConnection();

			// Set properties
			connexion.setRequestMethod("GET");
			//connexion.setRequestProperty("User-Agent", USER_AGENT);

			// Send request
			int code = connexion.getResponseCode();

			// Get response
			BufferedReader input = new BufferedReader(
				new InputStreamReader(connexion.getInputStream()));
			String line;
			StringBuffer response = new StringBuffer();

			while ((line = input.readLine()) != null) {
				response.append(line);
			}

			input.close();

			// Return response
			return response.toString();
			
		} catch (Exception e) {
			System.err.println("Error while sending request to tracker");
		}
		
	}

}