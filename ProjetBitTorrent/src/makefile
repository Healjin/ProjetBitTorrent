JCC = javac

JFLAGS = -cp ../libs/bee-encode-0.2.jar:./

default: ClientBitTorrent.class Metafile.class Torrent.class Peer.class Peers.class PeersManager.class PeerConnection.class

%.class: %.java
	$(JCC) $(JFLAGS) $<

clean: 
	$(RM) *.class