import java.security.MessageDigest;
import java.io.RandomAccessFile;

public class Torrent {

	private Metafile torrent = null;
	private String infoHash = null;
	private String peerID = null;
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
			tmp.setLength(1024 * 1024);
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
		
		// Set downloaded, uploaded and left values
		this.uploaded = 0;
		this.downloaded = 0;
		this.left = this.torrent.getLength();
		
		// Set event status
		this.event = "started";
		
	}
	
	public String Request(SortedMap params) {
		
		
		
	}

}
