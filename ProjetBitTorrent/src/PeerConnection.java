import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.nio.file.*;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

public class PeerConnection extends Thread {
	
	final int TIME_OUT_CONNECTION = 2000;
	final int TIME_OUT_HANDSHAKE = 2000;
	final int BUFFER_SIZE_INPUT = 65536;
	final int BLOCK_SIZE = 16384;
	final int TIME_BETWEEN_KEEPALIVE = 2; // minutes
	final int TIME_BETWEEN_LOGS = 10; // secondes

	int[] piecesDownloaded; // 0 = non downloaded, 1 = in progress , 2 = download finished
	int[] remotePiecesAvailables; // 0 = not available, 1 = available
	byte[] infoHash;
	String peerID;
	Metafile metafile;
	Peer peer;
	Socket socket;
	Boolean handshaken = false;
	Boolean choked = true;
	Boolean interested = false;
	int bytesReaded = 0;
	int blocksReceived = 0;
	byte[] piece;
	Boolean isAlive = false;
	Boolean isRunning = false;

	public PeerConnection(Peer peer, Metafile metafile, int[] piecesDownloaded, byte[] infoHash, String peerID) throws IOException {
		
		// Initialize attributes
		this.peer = peer;
		this.piecesDownloaded = piecesDownloaded;
		this.remotePiecesAvailables = new int[piecesDownloaded.length];
		this.metafile = metafile;
		this.infoHash = infoHash;
		this.peerID = peerID;
		
		// Initlialize connection to peer
		System.out.flush();
		socket = new Socket();
		socket.connect(new InetSocketAddress(peer.getIP(), peer.getPort()), TIME_OUT_CONNECTION);
		
		// Proceed to the handshake
		handshaken = handshake();
		
	}

	public Boolean handshake() {

		byte[] handshake = new byte[68];

		// Creation handshake frame
		final String HANDSHAKE = ((char) 19) + "BitTorrent protocol";
		System.arraycopy(HANDSHAKE.getBytes(), 0, handshake, 0, 20);
		System.arraycopy(infoHash, 0, handshake, 28, 20);
		System.arraycopy(peerID.getBytes(), 0, handshake, 48, 20);

		// Streams to interact with the socket
		InputStream mIn;
		OutputStream mOut;
		DataInputStream mDataIn;
		DataOutputStream mDataOut;

		try {
			
			// Retrieve all socket streams
			mOut = socket.getOutputStream();
			mIn = socket.getInputStream();
			mDataIn = new DataInputStream(mIn);
			mDataOut = new DataOutputStream(mOut);

			// Set time out value
			socket.setSoTimeout(TIME_OUT_HANDSHAKE);
			
			// Send handshake
			mDataOut.write(handshake);
			mDataOut.flush();

			// Wait response
			byte[] response = new byte[68];
			System.out.flush();

			// Read response from peer
			mDataIn.readFully(response);

			byte[] responseInfoHash = Arrays.copyOfRange(response, 28, 48);
			byte sizeProtocolName = response[0];

			// Make sure that is the bittorrent protocol
			if ((sizeProtocolName == 19 ) && new String(response).toLowerCase().contains("bittorrent protocol")) {
				
				// If the hash send is the same as the hash received
				if (Arrays.equals(infoHash, responseInfoHash)) {
					
					// Extract peer ID and store it
					String peerID = new String(Arrays.copyOfRange(response, 48, 68));
					peer.setPeerID(peerID);
					
					// Start keepConnectionAlive
					keepConnectionAlive kp = new keepConnectionAlive();
					kp.start();
					
					// Handshake is correct.
					return true;
					
				}
				
			}
			
			// Handshake is incorrect
			return false; 
			
		} catch (EOFException e) {
			// System.out.print("Data from " + peer.toString() + " are corrupted." + "\n");
			return false;
		} catch (IOException e) {
			// System.out.print("Error while tryin to read data input stream from " + peer.toString() + "\n");
			return false;
		}
		
	}
	
	// Return -1 if all pieces available on the peer are downloaded
	private int getFirstPieceMissing(){
		synchronized (piecesDownloaded) {
			for (int i = 0; i < piecesDownloaded.length; i++) {
				if((piecesDownloaded[i] == 0) && (remotePiecesAvailables[i] == 1)){
					piecesDownloaded[i] = 1;
					return i;
				}
			}
		}
		return -1; // No pieces missing
	}
	
	public void run() {
		
		System.out.println("Start download with : " + peer.getIP() + ":" + peer.getPort());
		
		isRunning = true;
		
		// Start logs
		writeLogs wl = new writeLogs();
		wl.start();
		
		// Start listening to peer
		ReceiveMessages rm = new ReceiveMessages();
		rm.start();
		
		/*try {
			
			byte[] bitfield = new byte[(int) Math.ceil(piecesDownloaded.length / 8)];

			// Set the bitfield
			for (int i = 0; i < piecesDownloaded.length; i++) {
				if (piecesDownloaded[i] == 1) {
					setBit(bitfield, i, piecesDownloaded[i]);
				}
			}

			// Construct message
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] msgLength = ByteBuffer.allocate(4).putInt(bitfield.length).array();
			out.write(msgLength);
			out.write(5);
			out.write(bitfield);
			out.flush();
			
			// Send bitfield
			SendMessage sm = new SendMessage(out.toByteArray());
			sm.start();
			
		} catch (Exception e) {
			System.err.println("Error while sending bitfield");
		}*/		
		
		byte[] interested = new byte[5];
		byte[] msgLength2 = ByteBuffer.allocate(4).putInt(1).array();
		System.arraycopy(msgLength2, 0, interested, 0, 4);
		interested[4] = 2;
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		try {
			out2.write(interested);
			out2.flush();
		} catch (IOException e) {
			System.err.println("Error while sending request");			
		}
		
		// Send bitfield
		SendMessage sm2 = new SendMessage(out2.toByteArray());
		sm2.start();
		
		int indexPieceMissing = -1;
		int blocksCount = (int) Math.floor(metafile.getPiece_length() / BLOCK_SIZE);
		int rest = metafile.getPiece_length() % BLOCK_SIZE;
		
		while(true) {
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				System.err.println("Error while trying to sleep");
			}

			int totalBlocksCount = blocksCount;
			blocksReceived = 0;
			
			if((choked == false) && ((indexPieceMissing = getFirstPieceMissing()) != -1))
			{

				System.out.println("Piece " + indexPieceMissing + " in progress ...");
				
				int pieceLength = metafile.getPiece_length();
				int lastPieceLength;
				
				if (indexPieceMissing == piecesDownloaded.length - 1) {
					if ((lastPieceLength = metafile.getLength() % pieceLength) != 0) {
						pieceLength = lastPieceLength;
					}
				}
				
				piece = new byte[pieceLength];
				for (int i = 0; i < pieceLength; i++) {
					piece[i] = (byte) 0xFF;
				}
				
				for (int i = 0; i < blocksCount; i++) {
				
					// Constructs the request
					byte[] request = new byte[17];
					byte[] msgLength = ByteBuffer.allocate(4).putInt(13).array();
					byte[] index = 	ByteBuffer.allocate(4).putInt(indexPieceMissing).array();
					byte[] begin = 	ByteBuffer.allocate(4).putInt(i * BLOCK_SIZE).array();
					byte[] length = ByteBuffer.allocate(4).putInt(BLOCK_SIZE).array();	

					System.arraycopy(msgLength, 0, request, 0, 4);
					request[4] = 6;
					System.arraycopy(index, 0, request, 5, 4);
					System.arraycopy(begin, 0, request, 9, 4);
					System.arraycopy(length, 0, request, 13, 4);
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					try {
						out.write(request);
						out.flush();
					} catch (IOException e) {
						System.err.println("Error while sending request");			
					}
					
					// Send request
					SendMessage sm = new SendMessage(out.toByteArray());
					sm.start();
					
				}
				
				if (rest != 0) {
					
					totalBlocksCount++;
						
					// Constructs the request
					byte[] request = new byte[17];
					byte[] msgLength = ByteBuffer.allocate(4).putInt(13).array();
					byte[] index = 	ByteBuffer.allocate(4).putInt(indexPieceMissing).array();
					byte[] begin = 	ByteBuffer.allocate(4).putInt(blocksCount * BLOCK_SIZE).array();
					byte[] length = ByteBuffer.allocate(4).putInt(rest).array();	

					System.arraycopy(msgLength, 0, request, 0, 4);
					request[4] = 6;
					System.arraycopy(index, 0, request, 5, 4);
					System.arraycopy(begin, 0, request, 9, 4);
					System.arraycopy(length, 0, request, 13, 4);
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					try {
						out.write(request);
						out.flush();
					} catch (IOException e) {
						System.err.println("Error while sending request");			
					}
					
					// Send request
					SendMessage sm = new SendMessage(out.toByteArray());
					sm.start();
					
				}
				
				while (blocksReceived < totalBlocksCount) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						System.err.println("Error while trying to sleep");
					}	
				}
				
				try {
					
					// Gets the sha1 of the piece
					MessageDigest md = MessageDigest.getInstance("SHA-1");
					md.update(piece);
					byte[] pieceHash = md.digest();

					// Gets the real sha1
					byte[] realPieceHash = new byte[20];
					System.arraycopy(metafile.getPieces(), indexPieceMissing * 20, realPieceHash, 0, 20);
					
					synchronized (piecesDownloaded) {
						// Checks the sha1
						if (java.util.Arrays.equals(realPieceHash,pieceHash)) {
							piecesDownloaded[indexPieceMissing] = 2;
							System.out.println("Piece " + indexPieceMissing + " downloaded successfully");
						} else {
							piecesDownloaded[indexPieceMissing] = 0;
							System.out.println("Piece " + indexPieceMissing + " not downloaded");
						}
					}

					try {
							
						// Writes the piece on file
						RandomAccessFile tmp = new RandomAccessFile(metafile.getName(), "rw");
						tmp.seek(indexPieceMissing * metafile.getPiece_length());
						tmp.write(piece, 0, pieceLength);
						
					} catch (Exception e) {
						System.err.println("Error while writing to file");
					}

				} catch (Exception e) {
					System.err.println("Error while checking piece's sha1");
				}
				
			}
			
		}
		
	}
	
	public class ProcessMessage extends Thread {
		
		byte[] msg;
		
		public ProcessMessage(byte[] msg) {
			this.msg = msg;
		}
		
		public void run() {	

			byte id = msg[4];
			
			switch (id) {
				
				case 0:
				
					choked = true;
					break;
				
				case 1:
				
					choked = false;
					break;
				
				case 2:
				
					interested = true;
					break;
				
				case 3:
				
					interested = false;
					break;
				
				case 4:
					
					// Actualise the remotePiecesAvailables array
					byte index = msg[5];
					if (index >= 0 && index <= remotePiecesAvailables.length) {
						remotePiecesAvailables[index] = 1;
					}
				
					break;
				
				case 5:
				
					// Set the remotePiecesAvailables array
					byte[] bitfield = Arrays.copyOfRange(msg, 5, msg.length);
					for (int i = 0; i < remotePiecesAvailables.length; i++) {
						remotePiecesAvailables[i] = getBit(bitfield, i);
					}
				
					break;
				
				case 6:
				
					break;
				
				case 7:

					// Gets the block's size
					byte[] msgLength = new byte[4];
					System.arraycopy(msg, 0, msgLength, 0, 4);
					int blockLength = ByteBuffer.wrap(msgLength).getInt() - 9;
				
					byte[] tmpIndexPiece = new byte[4];
					byte[] tmpBegin = new byte[4];
					byte[] block = new byte[blockLength];
					
					System.arraycopy(msg, 5, tmpIndexPiece, 0, 4);
					System.arraycopy(msg, 9, tmpBegin, 0, 4);
					System.arraycopy(msg, 13, block, 0, blockLength);
				
					int indexPiece = ByteBuffer.wrap(tmpIndexPiece).getInt();
					int begin = ByteBuffer.wrap(tmpBegin).getInt();
				
					if (metafile.isSingleFile()) {
						
						System.arraycopy(block, 0, piece, begin, blockLength);
						
					} else {
						System.out.println("Multifiles not supported");	
					}
				
					blocksReceived++;
				
					break;
				
				case 8:
					break;
				
			}

		}
		
	}
	
	// http://www.herongyang.com/Java/Bit-String-Get-Bit-from-Byte-Array.html
	public int getBit(byte[] data, int pos) {
		int posByte = pos / 8; 
		int posBit = pos % 8;
		byte valByte = data[posByte];
		int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
		return valInt;
	}
	
	// http://www.herongyang.com/Java/Bit-String-Set-Bit-to-Byte-Array.html
	private static void setBit(byte[] data, int pos, int val) {
		int posByte = pos/8; 
		int posBit = pos%8;
		byte oldByte = data[posByte];
		oldByte = (byte) (((0xFF7F>>posBit) & oldByte) & 0x00FF);
		byte newByte = (byte) ((val<<(8-(posBit+1))) | oldByte);
		data[posByte] = newByte;
	}
	
	public class ReceiveMessages extends Thread {

		public void run() {
			
			try {

				ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE_INPUT);
				int msgLength = 1500;
				DataInputStream input = new DataInputStream(socket.getInputStream());
				
				while (true) {
					Thread.sleep(50);
					// Check if some data is available
					int size = input.available();
					bytesReaded += size;
					
					// Some data is available
					if (size > 0) {
						
						// Get bytes on by one
						for (int i = 0; i < size; i++) {
							
							bb.put(input.readByte());
							
							// Get the length of the message
							if (bb.position() == 4) {
								msgLength = bb.getInt(0);
							}
							
							// Avoid "keep-alive" messages
							if (msgLength == 0) {
								bb.clear();	
							}
							
							// Get and process the message
							if (bb.position() == msgLength + 4) {
								byte[] msg = new byte[msgLength + 4];
								bb.rewind();
								bb.get(msg, 0, msgLength + 4);
								bb.clear();
								ProcessMessage pm = new ProcessMessage(msg);
								pm.start();
							}
							
						}
						
					}
					
				}
				
			} catch (Exception e) {
				System.err.println("Error while listening messages");
			}

		}

	}
	
	public class SendMessage extends Thread {
		
		byte[] msg;
		
		public SendMessage(byte[] msg) {
			this.msg = msg;
		}
		
		public void run() {
			
			try {
				
				// Send message
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				output.write(msg);
				output.flush();
				
			} catch (Exception e) {
				
				isAlive = false;
				System.out.println("Connection with : " + peer.getIP() + ":" + peer.getPort() + " is down");

			}
			
		}
		
	}
	
	public class keepConnectionAlive extends Thread {
		
		public void run() {
			
			isAlive = true;
			
			while (isAlive) {
		
				byte[] msg = ByteBuffer.allocate(4).putInt(0).array();
				SendMessage sm = new SendMessage(msg);
				sm.start();
				
				try {
					Thread.sleep(1000 * TIME_BETWEEN_KEEPALIVE);
				} catch (InterruptedException e) {
					System.err.println("Error while trying to sleep");
				}	
				
			}
			
		}
		
	}
	
	public class writeLogs extends Thread {
		
		public void run() {
			
			File file = new File(metafile.getName() + ".logs.txt");
			
			while (true) {
							
				try {
					Thread.sleep(1000 * TIME_BETWEEN_LOGS);
				} catch (InterruptedException e) {
					System.err.println("Error while trying to sleep");
				}	

				Locale locale = Locale.getDefault();
				DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
				int speed = bytesReaded / TIME_BETWEEN_LOGS / 1024; // KB/seconds
				bytesReaded = 0;
				String remotePieces = "";
				
				for (int i = 0; i < remotePiecesAvailables.length; i++) {
					if (remotePiecesAvailables[i] == 0) {
						remotePieces += i + ":";
					}
				}
				
				if (remotePieces.equals("")) {
					remotePieces = "none";
				}
				
				try {

					BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
					output.write(dateFormat.format(new Date()) + ",");
					output.write(peer.getIP() + ":" + peer.getPort() + ",");
					output.write(peer.getPeerID() + ",");
					output.write(speed + ",");
					//output.write(remotePieces + ",");
					output.write("\n");
					output.close();
					
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}

}
