/*-------------------------------------------------------------------------
	FILE		: 	Metafile.java
	DESCRIPTION	:	This class is used to extract the data in the metafile 
					file. Once extracted data can be retrieved via getters.
	AUTHORS		:	Magnin Antoine, Da Silva Andrade David
-------------------------------------------------------------------------*/
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ardverk.coding.BencodingInputStream;

public class Metafile {

	// Main vars
	private String announce;
	private ArrayList<String> announce_list;
	private String comment;
	private String createdBy;
	private Date creationDate;
	private TreeMap<String, ?> info; // Dictionary
	private String name;
	private Integer piece_length;
	private byte[] pieces;
	private String source;

	// Single file
	private Integer length;

	// Multi file
	private ArrayList<Map<String, ?>> files;

	boolean multiFile = false;

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Constructor that will extract all values from the file
						given in parameter.
		PARAMS		:	(String) file
		RETURN		:	None
	-------------------------------------------------------------------------*/
	@SuppressWarnings({ "unchecked" })
	public Metafile(String file) {
		
		this.source = file;
		
		BencodingInputStream bencodeDecoder;
		try {
			bencodeDecoder = new BencodingInputStream(new FileInputStream(file));
			Map<String, ?> fileContent = bencodeDecoder.readMap();

			if (fileContent.get("announce") != null)
				announce = new String((byte[]) fileContent.get("announce"));

			if (fileContent.get("announce-list") != null) {
				announce_list = new ArrayList<String>();
				List<List<byte[]>> tmp_list = (List<List<byte[]>>) fileContent.get("announce-list");
				for (int i = 0; i < tmp_list.get(0).size(); i++)
					announce_list.add(new String((byte[]) tmp_list.get(0).get(i)));
			}
			
			if (fileContent.get("comment") != null)
				comment = new String((byte[]) fileContent.get("comment"));
			
			if (fileContent.get("created_by") != null)
				createdBy = new String((byte[]) fileContent.get("created_by"));
			
			if (fileContent.get("creation date") != null)
				creationDate = new java.util.Date((long) ((BigInteger) fileContent.get("creation date")).intValue() * 1000);
			
			if (fileContent.get("name") != null)
				name = new String((byte[]) fileContent.get("name"));

			if (fileContent.get("info") != null) {
				info = (TreeMap<String, ?>) fileContent.get("info");
				if (info.containsKey("files")) {
					multiFile = true;
					files = (ArrayList<Map<String, ?>>) info.get("files");

					System.out.println(files);
				} else if (info.containsKey("length")) {
					multiFile = false;
					length = ((BigInteger) info.get("length")).intValue();
				}

				if (info.containsKey("name")) {
					name = new String((byte[]) info.get("name"));
				}
				if (info.containsKey("piece length")) {
					piece_length = ((BigInteger) info.get("piece length")).intValue();
				}
				if (info.containsKey("pieces")) {
					pieces = (byte[]) info.get("pieces");
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Torrent file not found. Please check your path.");
			// Leave program
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Error while trying to extract data from metafile");
			// Leave program
			System.exit(1);
		}
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Getters to access data from metafile.
	-------------------------------------------------------------------------*/
	public String getAnnounce() {
		return announce;
	}

	public ArrayList<String> getAnnounce_list() {
		return announce_list;
	}

	public String getComment() {
		return comment;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public TreeMap<String, ?> getInfo() {
		return info;
	}

	public String getName() {
		return name;
	}

	public Integer getPiece_length() {
		return piece_length;
	}

	public byte[] getPieces() {
		return pieces;
	}

	public Integer getLength() {
		return length;
	}

	public Boolean isMultiFile() {
		return multiFile;
	}

	public ArrayList<Map<String, ?>> getFiles() {
		return files;
	}

	public String getSource() {
		return source;
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	return true if torrent is multi-files otherwise return
						false
		PARAMS		:	None
		RETURN		:	Boolean
	-------------------------------------------------------------------------*/
	public Boolean isSingleFile() {
		return !multiFile;
	}
}
