import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;

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
			tmp.setLength((long) torrent.getLength());
			tmp.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error while creating tmp file");
		}

		// Get SHA-1 infoHash
		try {
			File fileS = new File("./test2.torrent");
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			InputStream input = null;
			try {
				input = new FileInputStream(fileS);
				StringBuilder builder = new StringBuilder();
				while (!builder.toString().endsWith("4:info")) {
					builder.append((char) input.read()); // It's ASCII anyway.
				}
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				for (int data; (data = input.read()) > -1; output.write(data)); // A CHANGER 
				sha1.update(output.toByteArray(), 0, output.size() - 1);
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException ignore) {
					}
			}
//%CB%84%EF%BF%BD%EF%BF%BD%0F%29m%EF%BF%BD-l%40%EF%BF%BDz%07%EF%BF%BDx%EF%BF%BD2%3A%14
//%C3%8B%C2%84%C3%8C%C3%81%0F%29m%C3%B7-l%40%C2%BAz%07%C3%81x%C2%A42%3A%14
//%CB%84%CC%C1%0F%29%6D%F7%2D%6C%40%BA%7A%07%C1%78%A4%32%3A%14
//%CB%84%CC%C1%0F%29m%F7-l%40%BAz%07%C1x%A42%3A%14
			byte[] hash = sha1.digest(); // Here's your hash. Do your thing with it.
			//this.infoHash = URLEncoder.encode(new String(hash,"UTF-8"), "UTF-8");
			this.infoHash = encodeURL(javax.xml.bind.DatatypeConverter.printHexBinary(hash));
			System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(hash));
			
			/*byte[] encoded = Files.readAllBytes(Paths.get("./test3.torrent"));
			String fileContent = new String(encoded, "UTF-8");
			int index = fileContent.indexOf("e4:info") + 7;
			byte[] infoValue = new byte[encoded.length - index];
			System.arraycopy(encoded, index, infoValue, 0, infoValue.length-1);
			System.out.println(infoValue.length);

			MessageDigest md = null;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1 = md.digest(infoValue);

			String testSt = new String(javax.xml.bind.DatatypeConverter.parseHexBinary("cb84ccc10f296df72d6c40ba7a07c178a4323a14"), "UTF-8");
			System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(sha1));
			this.infoHash = URLEncoder.encode(new String(sha1), "UTF-8");*/
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

	public String request() {
		StringBuffer response = null;
		try {

			// Initialize http connexion
			URL url = new URL(torrent.getAnnounce() + "?info_hash=" + this.infoHash + "&peer_id=" + this.peerID + "&port=" + this.port + "&uploaded="
					+ this.uploaded + "&downloaded=" + this.downloaded + "&left=" + this.left + "&event=" + this.event);
			HttpURLConnection connexion = (HttpURLConnection) url.openConnection();
			System.out.println(url);
			// Set properties
			connexion.setRequestMethod("GET");
			// connexion.setRequestProperty("User-Agent", USER_AGENT);

			// Send request
			int code = connexion.getResponseCode();
			System.out.println(code);

			// Get response
			BufferedReader input = new BufferedReader(new InputStreamReader(connexion.getInputStream()));
			String line;
			response = new StringBuffer();

			while ((line = input.readLine()) != null) {
				response.append(line);
			}

			input.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error while sending request to tracker");
		}

		// Return response
		return response.toString();
	}

	public static String encodeURL(String hexString) throws Exception {
		if (hexString == null || hexString.isEmpty()) {
			return "";
		}
		if (hexString.length() % 2 != 0) {
			throw new Exception("String is not hex, length NOT divisible by 2: " + hexString);
		}
		int len = hexString.length();
		char[] output = new char[len + len / 2];
		int i = 0;
		int j = 0;
		while (i < len) {
			output[j++] = '%';
			output[j++] = hexString.charAt(i++);
			output[j++] = hexString.charAt(i++);
		}
		return new String(output);
	}
}
