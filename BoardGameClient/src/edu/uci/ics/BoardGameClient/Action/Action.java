package edu.uci.ics.BoardGameClient.Action;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.uci.ics.BoardGameClient.Board.*;
import edu.uci.ics.BoardGameClient.Common.Definitions;
import edu.uci.ics.BoardGameClient.Common.Message;
import edu.uci.ics.BoardGameClient.Engine.Game;
import edu.uci.ics.BoardGameClient.GUI.GUI;

public class Action {

	private Game game;
	private int gameType;
	private Board board;
	private BoardManipulator manipulator;
	private ActionReactor reactor; // abstract
	private GameObjectFactory gof;
	private MoveValidator validator; // abstract How are we to use this?
	private int numberOfPlayers;
	private GUI gui;

	public Action(Game game) {
		this.game = game;
		int gameType = -1; 
		int numberOfPlayers = -1;
		
		Object[] options = { "TicTacToe" };
		int n = JOptionPane.showOptionDialog(null, "Please select a game",
				"Status", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == 0) {  //TicTacToe was selected
			gameType = Definitions.GAMETYPETICTACTOE;
			numberOfPlayers = 2;
		} 
		
		createGame(gameType);
	}

	
	private void setUp(int gameType, int numberOfPlayers){
		this.gameType = gameType;
		gof = new GameObjectFactory();
		
		board = createBoard(gameType, numberOfPlayers);
		manipulator = new BoardManipulator(board, game, this);
		reactor = setActionReactor();
		
		gui = new GUI(this, board, gameType);
	}
	
	public void createGame(int gameType) {
		game.createGame(gameType);
	}
		
	public void setGame(Game game) {
		this.game = game;
	}

	public int getGameType() {
		return gameType;
	}
	
	public Game getGame() {
		return game;
	}

	public void setNumberOfPlayers(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}

	public void shutdown() {
		game.shutdown();
	}

	public void messageFromServer(Message message) {
		JSONObject gameMessage;
		// The client receives messages from the server and implements them
		try {
			gameMessage = (JSONObject) new JSONParser().parse(message.message);
			
			if (gameMessage.get("MessageType").equals("BoardCreated")) { // Game has been created
				setUp(gameType, numberOfPlayers);
			}
			else if (gameMessage.get("MessageType").equals("Create")) { // Client Side create game object

				Integer gameType = new Integer(((Long) gameMessage.get("GameType")).intValue());
				Integer playerID = new Integer(((Long) gameMessage.get("PlayerID")).intValue());
				Integer row = new Integer(((Long) gameMessage.get("Row")).intValue());
				Integer col = new Integer(((Long) gameMessage.get("Col")).intValue());
				Integer gameID = new Integer(((Long) gameMessage.get("GameID")).intValue());
				Integer objectID = new Integer(((Long) gameMessage.get("ObjectID")).intValue());
				Integer objectType = new Integer(((Long) gameMessage.get("ObjectType")).intValue());

				manipulator.createGameObject(gof.createGameObject(playerID, objectType, objectID, row, col), gameID,
						gameType, objectID, objectType, playerID, row, col);

				reactor.updateBoard();
				
				gui.update();

			} else if (gameMessage.get("MessageType").equals("Delete")) { // Client Side delete game object
				Integer playerID = new Integer(((Long) gameMessage.get("PlayerID")).intValue());
				Integer gameID = new Integer(((Long) gameMessage.get("GameID")).intValue());
				Integer objectID = new Integer(((Long) gameMessage.get("ObjectID")).intValue());

				manipulator.deleteGameObject(objectID, gameID, playerID);

				reactor.updateBoard();
				
				gui.update();

			} else if (gameMessage.get("MessageType").equals("Move")) { // Client Side move game object
				Integer playerID = new Integer(((Long) gameMessage.get("PlayerID")).intValue());
				Integer objectID = new Integer(((Long) gameMessage.get("ObjectID")).intValue());
				Integer gameID = new Integer(((Long) gameMessage.get("GameID")).intValue());
				Integer row = new Integer(((Long) gameMessage.get("Row")).intValue());
				Integer col = new Integer(((Long) gameMessage.get("Col")).intValue());

				manipulator.moveGameObject(objectID, playerID, row, col, gameID);

				reactor.updateBoard();
				
				gui.update();

			} else if (gameMessage.get("MessageType").equals("Swap")) { // Client Side swap game object
				Integer objectID1 = new Integer(((Long) gameMessage.get("ObjectID1")).intValue());
				Integer objectID2 = new Integer(((Long) gameMessage.get("ObjectID2")).intValue());
				Integer gameID = new Integer(((Long) gameMessage.get("GameID")).intValue());
				Integer playerID = new Integer(((Long) gameMessage.get("PlayerID")).intValue());

				manipulator.swapGameObjects(objectID1, objectID2, playerID, gameID);

				reactor.updateBoard();

				gui.update();
				
			} else if (gameMessage.get("MessageType").equals("Win")) { // Client Side recognize winners
				gui.winMessage();
			} else if (gameMessage.get("MessageType").equals("Lose")) { // Client Side recognize losers
				gui.loseMessage();
			} else if (gameMessage.get("MessageType").equals("Tie")) { // Client Side recognizes a tie
				gui.tieMessage();
			} else if (gameMessage.get("MessageType").equals("InvalidMove")) { // Client Side invalid move handling
				// GUI should display that chosen move is invalid
			}

		} catch (ParseException e) {
			// Catch a parse exception for JSON object
			e.printStackTrace();
		}

	}

	public Message encodeMessage(JSONObject gameMessage) {

		Message messageToServer = new Message();
		messageToServer.message = gameMessage.toJSONString();

		return messageToServer;

	}

	private Board createBoard(int gameType, int playerNum) // Perhaps encapsulate this in its own object?
	{
		if (gameType == Definitions.GAMETYPETICTACTOE) {
			int TicTacToe_BoardHeight = 3;
			int TicTacToe_BoardWidth = 3;

			return new Board(TicTacToe_BoardHeight, TicTacToe_BoardWidth, Definitions.GAMETYPETICTACTOE);
		}

		return null;
	}

	private MoveValidator setMoveValidator() // Perhaps encapsulate this in its own object?
	{
		if (gameType == Definitions.GAMETYPETICTACTOE) {
			return new TicTacToeValidator(board);
		}
		return null;
	}

	private ActionReactor setActionReactor() // Perhaps encapsulate this in its own object?
	{
		if (gameType == Definitions.GAMETYPETICTACTOE) {
			return new TicTacToeReactor(gof, manipulator);
		} else {
			return null;
		}
	}
}
