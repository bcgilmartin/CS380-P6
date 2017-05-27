import java.io.InputStream;
import java.net.Socket;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;

public class TicTacToeClient {

	public static void main(String[] args) throws Exception {
		
		//connecting to socket and setup io
        try (Socket socket = new Socket("codebank.xyz", 38006)) {
			System.out.println("\nConnected to server.");
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			ObjectInputStream ois = new ObjectInputStream(is);
			Scanner kb = new Scanner(System.in);
			
			
			//Send Connect Message to identify yourself to the server
			oos.writeObject(new ConnectMessage("Blake"));
			
			
			//Send Command Message to start a new game with server
			oos.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));
			
			
			//Receive Board from server
			BoardMessage board = (BoardMessage)ois.readObject();
			
			
			//Create variables
			Object serverMessage;
			ErrorMessage error = null;
			int row;
			int column;
			
			
			//Play the game until someone wins or stalemate or error occurs
			while(board.getStatus() == BoardMessage.Status.IN_PROGRESS && error == null) {
				
				//prints the current board
				printBoard(board);
				
				
				//ask for move and send move
				System.out.print("Your move(rows and columns are from 0-2):\nEnter row: ");
				row = kb.nextInt();
				System.out.print("Enter column: ");
				column = kb.nextInt();
				oos.writeObject(new MoveMessage((byte)row, (byte)column));
				
				
				//receive server message and identify type
				serverMessage = ois.readObject();
				if(serverMessage instanceof BoardMessage) {
					board = (BoardMessage)serverMessage;
				} else if(serverMessage instanceof ErrorMessage) {
					error = (ErrorMessage)serverMessage;
				}	
			}
			
			//display what has happened with the game
			if(error != null) {
				System.out.println(error.getError());
			} else {
				System.out.print("Game Complete: ");
				if(BoardMessage.Status.PLAYER1_VICTORY == board.getStatus()) {
					System.out.println("You Win");
				} else if(BoardMessage.Status.PLAYER2_VICTORY == board.getStatus()) {
					System.out.println("Server Wins");
				} else {
					System.out.println("Stalemate");
				}
			}
        }
	}
	
	
	//print out the board contained in the BoardMessage
	public static void printBoard(BoardMessage board) {
		byte[][] byteBoard = board.getBoard();
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				switch((int)byteBoard[x][y]) {
					case 0:
						System.out.print(" ");
						break;
					case 1:
						System.out.print("X");
						break;
					case 2:
						System.out.print("O");
				}
				if(y != 2)
					System.out.print("|");
			}
			System.out.println();
		}
	}
	
}