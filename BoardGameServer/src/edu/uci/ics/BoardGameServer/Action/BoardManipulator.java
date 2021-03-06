package edu.uci.ics.BoardGameServer.Action;

import org.json.simple.JSONObject;

import edu.uci.ics.BoardGameServer.Board.Board;
import edu.uci.ics.BoardGameServer.Board.GameObject;
import edu.uci.ics.BoardGameServer.Engine.Game;

public class BoardManipulator {

	private Action action;
	private Board board;
	private Game game;
	
	
	
	public BoardManipulator(Board board, Game game, Action action)
	{
		this.board = board;
		this.action = action; // need action for message passing
		this.game = game; // need game for message passing
	}
	
	@SuppressWarnings("unchecked")
	public void createGameObject(GameObject g, Integer gameID, Integer gameType, Integer objectID, Integer objectType, Integer playerNum, Integer row, Integer col, Integer numberOfPlayers)
	{	
		board.addToBoard(g, row, col);
		
		
		for(int i=0; i<numberOfPlayers; i++)
		{
			JSONObject gameMessage=new JSONObject();
			  gameMessage.put("MessageType", "Create");
			  gameMessage.put("GameID", gameID);
			  gameMessage.put("GameType", gameType);
			  gameMessage.put("PlayerID", i);
			  gameMessage.put("ObjectID", objectID);
			  gameMessage.put("Row", row);
			  gameMessage.put("Col", col);
			  gameMessage.put("ObjectType", objectType);
			  
			game.messageToClient(action.encodeMessage(gameMessage)); // Send creation message to client
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void deleteGameObject(Integer objectID, Integer gameID, Integer playerNum, Integer numberOfPlayers)
	{
		board.removeFromBoard(objectID);
		
		for(int i=0; i<numberOfPlayers; i++)
		{
			JSONObject gameMessage=new JSONObject();
			  gameMessage.put("MessageType", "Delete");
			  gameMessage.put("GameID", gameID);
			  gameMessage.put("PlayerID", i);
			  gameMessage.put("ObjectID", objectID);

			game.messageToClient(action.encodeMessage(gameMessage)); // Send removal message to client
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void moveGameObject(Integer objectID, Integer playerNum, Integer row, Integer col, Integer gameID, Integer numberOfPlayers)
	{
		board.move(objectID, row, col);
	
		for(int i=0; i<numberOfPlayers; i++)
		{
			JSONObject gameMessage=new JSONObject();
			  gameMessage.put("MessageType", "Move");
			  gameMessage.put("GameID", gameID);
			  gameMessage.put("PlayerID", i);
			  gameMessage.put("ObjectID", objectID);
			  gameMessage.put("Row", row);
			  gameMessage.put("Col", col);
			  
			game.messageToClient(action.encodeMessage(gameMessage)); // Send move message to client
		}
	}
	
	@SuppressWarnings("unchecked")
	public void swapGameObjects(Integer objectID1, Integer objectID2, Integer playerNum, Integer gameID, Integer numberOfPlayers)
	{
		board.swap(objectID1, objectID2);
		
		
		  for(int i=0; i<numberOfPlayers; i++)
		  {
			  JSONObject gameMessage=new JSONObject();
			  gameMessage.put("MessageType", "Swap");
			  gameMessage.put("GameID", gameID);
			  gameMessage.put("PlayerID", i);
			  gameMessage.put("ObjectID1", objectID1);
			  gameMessage.put("ObjectID2", objectID2);
			
			  game.messageToClient(action.encodeMessage(gameMessage)); // Send swap message to client
		  }
		  
	}
	
	
	
	
	
	
	
	
	
}
