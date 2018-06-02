package simplegame;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 *
 * @author igor
 */
public class Player{
    private static final String WINDOW_TITLE = "Simple networked game";
    private int width, height;
    private JFrame frame;
    private JButton[] buttons = new JButton[4];
    private JTextArea messagesArea;
    private Container container;
    private ClientSideConnection clientSideConnection;
    private int id, opponentId;
    
    public Player(int width, int height){
        this.width = width;
        this.height = height;
        this.frame = new JFrame();
        for(int i = 0; i < 4; ++i){
            this.buttons[i] = new JButton(String.valueOf(i + 1));
            this.buttons[i].setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC,
                                             32));
        }
        this.messagesArea = new JTextArea();
        this.messagesArea.setText("Simple turn based game");
        this.container = this.frame.getContentPane();
        //setupGUI();
    }
    
    public final void setupGUI(){
        this.frame.setSize(width, height);
        this.frame.setTitle(String.format("Simple networked game: Player #%d",
                id));
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.messagesArea.setWrapStyleWord(true);
        this.messagesArea.setLineWrap(true);
        this.messagesArea.setEditable(false);
        this.container.setLayout(new GridLayout(1, 5));
        this.container.add(this.messagesArea);        
        for(JButton btn: buttons){
            this.container.add(btn);
        }
        this.frame.setLocationRelativeTo(null);
        
        if(id == 1){
            opponentId = 2;
            this.messagesArea.setText("You are player #1. You go first.");
        } else {
            opponentId = 1;
            this.messagesArea.setText("You are player #2.Wait for your turn.");
        }
        this.frame.setVisible(true);
    }
    
    public void connectToServer(String host, int port){
        clientSideConnection = new ClientSideConnection(host, port);
    }
    
    private class ClientSideConnection {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        
        public ClientSideConnection(String serverIP, int port){
            System.out.println("Client side connection");
            try {
                this.socket = new Socket(serverIP, port);
                this.dis = new DataInputStream(this.socket.getInputStream());
                this.dos = new DataOutputStream(this.socket.getOutputStream());
                id = dis.readInt();
                System.out.println(String.format("Connected to the server as"
                        + " Player #%d", id));
            } catch (IOException ex) {
                Logger.getLogger(ClientSideConnection.class.getName())
                        .log(Level.SEVERE, "IOException from"
                                + " ClientSideConnection constructor", ex);
            }
        }
    }
    
    public static void main(String[] args) {
        Player player = new Player(500, 100);
        player.connectToServer("127.0.0.1", 55555);
        player.setupGUI();
    }
}
