

public class ClientBitTorrent {

	public static void main(String[] args) {
		Metafile testMetafile = new Metafile("./test2.torrent");
		
		Torrent testTorrent = new Torrent(testMetafile);
		String request = testTorrent.request();
		System.out.println(request);
	}
}
