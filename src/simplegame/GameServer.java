package simplegame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author igor
 */
public class GameServer {
    private static final int NUM_PLAYER_MAX = 2;
    private ServerSocket serverSocket;
    private int numPlayers;

    public GameServer() {
        System.out.println("----Game server----");
        numPlayers = 0;
        try {
            serverSocket = new ServerSocket(3333);
        } catch (IOException ex) {
            Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.out.println("IOException from game server constructor");
        }
    }
    
    private void acceptConnections(){
        try {
            System.out.println("Waiting for players...");
            while(numPlayers < NUM_PLAYER_MAX){
                Socket socket = serverSocket.accept();
                ++numPlayers;
                System.out.println(String.format("Player #%n connected",
                        numPlayers));
            }
            System.out.println("Two players connected.No longer accepting "
                    + "connections.");
        } catch (IOException ex) {
            Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.out.println("IOException from acceptConnections.");
        }
        
    }
    
    
}
