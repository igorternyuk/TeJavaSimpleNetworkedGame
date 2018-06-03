package simplegame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author igor
 */
public class GameServer {
    private static final int NUM_PLAYER_MAX = 2;
    private static final int NUM_OF_VALUES = 4;
    private static final int MAX_VALUE = 100;
    private int port;
    private ServerSocket serverSocket;
    private int numPlayers;
    private ServerSideConnection player1, player2;
    private Random random = new Random();
    private int maxTurns;
    private int turnsMade;
    private int[] values = new int[NUM_OF_VALUES];
    private int player1ClickedButtonNumber;
    private int player2ClickedButtonNumber;
    
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
        this.maxTurns = 4;
        this.turnsMade = 0;
        generateRandomValues();
    }
    
    private void generateRandomValues(){
        System.out.println("----Generating random values----");
        for(int i = 0; i < this.values.length; ++i){
            this.values[i] = random.nextInt(MAX_VALUE);
            System.out.println("Value #" + (i + 1) + " is " + this.values[i]);
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
        private int playerID;
        private DataInputStream dis;
        private DataOutputStream dos;
        
        public ServerSideConnection(Socket socket, int playerID){
            this.socket = socket;
            this.playerID = playerID;
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
                this.dos.writeInt(this.playerID);
                this.dos.writeInt(maxTurns);
                this.dos.writeInt(turnsMade);
                for(int val: values){
                    this.dos.writeInt(val);
                }
                this.dos.flush();
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName())
                        .log(Level.SEVERE, "IOEsception from run() in"
                                + " ServerSideConnection", ex);
            }
            
            while(true){
                try{
                    if(playerID == 1){
                        player1ClickedButtonNumber = this.dis.readInt();
                        System.out.println("Player #1 clicked button #"
                            + player1ClickedButtonNumber);
                        player2.sendButtonNumber(player1ClickedButtonNumber);
                    } else {
                        player2ClickedButtonNumber = this.dis.readInt();
                        System.out.println("Player #2 clicked button #"
                            + player2ClickedButtonNumber);
                        player1.sendButtonNumber(player2ClickedButtonNumber);
                    }
                    ++turnsMade;
                    if(turnsMade == maxTurns){
                        System.out.println("---GAME OVER---");
                        break;
                    }
                } catch(IOException ex){
                    Logger.getLogger(GameServer.class.getName())
                        .log(Level.SEVERE, "IOEsception from run() in"
                                + " ServerSideConnection", ex);
                }
            }
            
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        
        public void sendButtonNumber(int buttonNumber){
            try {
                this.dos.writeInt(buttonNumber);
                this.dos.flush();
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer(55555);
        server.acceptConnections();
    }
}
