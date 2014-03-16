package edu.uci.ics.BoardGameServer.Distribution;

import edu.uci.ics.BoardGameServer.Common.Message;
import edu.uci.ics.BoardGameServer.Engine.TalkDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Distribution implements Runnable {

	private TalkDistribution talkDistribution;
	private volatile boolean stopRunning = false;
	private Server server;
	private Thread threadServer;
	private List<ClientWait> clientWaits = new ArrayList<ClientWait>();
	private List<ClientGame> clientGames = new ArrayList<ClientGame>();
	private Map<Integer, ClientGame> connectionToClientGame = new TreeMap<Integer, ClientGame>();
	private Map<Integer, ClientGame> gameIdToClientGame = new TreeMap<Integer, ClientGame>();

	public void setTalkDistribution(TalkDistribution talkDistribution) {
		this.talkDistribution = talkDistribution;
	}

	public void stop() {
		stopRunning = true;
	}

	@Override
	public void run() {

		server = new Server();
		server.setDistribution(this);

		threadServer = new Thread(server);
		threadServer.start();

		while (!stopRunning) {
			talkDistribution.waitForOutputQueue();
			messageToClient(talkDistribution.getOutputQueue());
		}

		server.stop();
		threadServer.interrupt();

	}

	public void createGame(int connectionId, String message) {
		message = message.replace("createGame ", "");
		int gameType = Integer.parseInt(message);

		// TODO: fix number of player depending on game type and etc
		ClientWait clientFound = null;
		for (ClientWait clientWaiting : clientWaits) {
			if (clientWaiting.gameType == gameType) {
				clientFound = clientWaiting;
			}
		}
		if (clientFound == null) {
			clientFound = new ClientWait();
			clientFound.connectionId = connectionId;
			clientFound.gameType = gameType;
			clientWaits.add(clientFound);
			return;
		}

		ClientGame clientGame = new ClientGame();
		clientGame.gameId = talkDistribution.createGame(gameType, 2);
		clientGame.connectionIds.add(connectionId);
		clientGame.connectionIds.add(clientFound.connectionId);
		connectionToClientGame.put(connectionId, clientGame);
		connectionToClientGame.put(clientFound.connectionId, clientGame);
		gameIdToClientGame.put(clientGame.gameId, clientGame);

		clientGames.add(clientGame);
	}

	// TODO: need to call destroyGame
	public void destroyGame() {
		// TODO: fix to destroy game correctly
		talkDistribution.destroyGame(2);
	}

	public void messageFromClient(int connectionId, String data) {
		ClientGame clientGame = connectionToClientGame.get(connectionId);
		
		Message message = new Message();
		message.gameId = clientGame.gameId;
		for (int i = 0; i < clientGame.connectionIds.size(); i++) {
			if (clientGame.connectionIds.get(i) == connectionId) {
				message.playerNumber = i;
			}
		}
		message.message = data;
		
		talkDistribution.messageFromClient(message);
	}

	private void messageToClient(Message message) {
		if (message == null) {
			return;
		}
		ClientGame clientGame = gameIdToClientGame.get(message.gameId);
		int connectionId = clientGame.connectionIds.get(message.playerNumber);
		server.messageToClient(connectionId, message.message);
	}
}
