package simplegame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    private int port;
    private ServerSocket serverSocket;
    private int numPlayers;
    private ServerSideConnection player1, player2;
    
    public GameServer(int port) {
        System.out.println("----Game server----");
        this.port = port;
        numPlayers = 0;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException ex) {
            Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.out.println("IOException from game server constructor");
        }
    }
    
    public int getPort(){
        return this.port;
    }
    
    private void acceptConnections(){
        try {
            System.out.println("Waiting for players...");
            while(numPlayers < NUM_PLAYER_MAX){
                Socket socket = serverSocket.accept();
                ++numPlayers;
                System.out.println(String.format("Player #%d connected",
                        numPlayers));
                ServerSideConnection ssc = new ServerSideConnection(socket,
                        numPlayers);
                if(numPlayers == 1){
                    this.player1 = ssc;
                } else {
                    this.player2 = ssc;
                }
                Thread thread = new Thread(ssc);
                thread.start();
            }
            System.out.println("Two players connected.No longer accepting "
                    + "connections.");
        } catch (IOException ex) {
            Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                    null, ex);
            System.out.println("IOException from acceptConnections.");
        }
        
    }
    
    private class ServerSideConnection implements Runnable{
        private Socket socket;
        private int PlayerID;
        private DataInputStream dis;
        private DataOutputStream dos;
        
        public ServerSideConnection(Socket socket, int playerID){
            this.socket = socket;
            this.PlayerID = playerID;
            try{
                this.dis = new DataInputStream(this.socket.getInputStream());
                this.dos = new DataOutputStream(this.socket.getOutputStream());
            } catch (IOException ex){
                Logger.getLogger(ServerSideConnection.class.getName())
                        .log(Level.SEVERE,
                             "IOException from ServerSideConnection constructor",
                             ex);
            }
            
        }
        
        @Override
        public void run() {
            try {
                this.dos.writeInt(this.PlayerID);
                this.dos.flush();
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName())
                        .log(Level.SEVERE, "IOEsception from run() in"
                                + " ServerSideConnection", ex);
            }
            
            while(true){
                    
            }
        }
        
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer(55555);
        server.acceptConnections();
    }
}
