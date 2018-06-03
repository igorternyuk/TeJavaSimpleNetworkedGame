package simplegame;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private int myPoints, opponentPoints;
    private int id, opponentId;
    private int maxTurns;
    private int turnsMade;
    private int values[] = new int[4];
    private boolean buttonsEnabled = false;
    private boolean[] alreadyClickedButtons = new boolean[4];
    
    public Player(int width, int height){
        this.width = width;
        this.height = height;
        this.frame = new JFrame();
        this.messagesArea = new JTextArea();
        this.messagesArea.setText("Simple turn based game");
        this.container = this.frame.getContentPane();
        myPoints = 0;
        opponentPoints = 0;
        for(boolean val: alreadyClickedButtons){
            val = false;
        }
    }
    
    public final void setupGUI(){
        this.frame.setSize(width, height);
        this.frame.setTitle(String.format("Simple networked game: Player #%d",
                id));
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.messagesArea.setWrapStyleWord(true);
        this.messagesArea.setLineWrap(true);
        this.messagesArea.setEditable(false);
        this.container.setLayout(new GridLayout(1, this.buttons.length + 1));
        this.container.add(this.messagesArea);        
        
        for(JButton btn: buttons){
            this.container.add(btn);
        }
        
        this.frame.setLocationRelativeTo(null);
        
        if(id == 1){
            opponentId = 2;
            buttonsEnabled = true;
            this.messagesArea.setText("You are player #1. You go first.");
        } else {
            opponentId = 1;
            buttonsEnabled = false;
            this.messagesArea.setText("You are player #2.Wait for your turn.");
            startWaitingForOpponentClickedButtonNumber();
        }
        
        toggleButtons();
        this.frame.setVisible(true);
    }
    
    public void connectToServer(String host, int port){
        clientSideConnection = new ClientSideConnection(host, port);
    }
    
    public final void setupButtons(){
        for(int i = 0; i < this.buttons.length; ++i){
            this.buttons[i] = new JButton(String.valueOf(i + 1));
            this.buttons[i].setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC,
                                             32));
            this.buttons[i].addActionListener((ActionEvent e) -> {
                JButton clickedButton = (JButton)e.getSource();
                int btnNumber = Integer.valueOf(clickedButton.getText());
                alreadyClickedButtons[btnNumber - 1] = true;
                System.out.println("Player #" + id + " clicked button #"
                        + btnNumber);
                clientSideConnection.sendButtonNumber(btnNumber);
                myPoints += Player.this.values[btnNumber - 1];
                System.out.println("Your points are " + myPoints);
                Player.this.messagesArea.setText("You clicked button #"
                        + btnNumber + "Your points are " + myPoints
                        + " Wait for player #" + opponentId);
                if(id == 2 && turnsMade == maxTurns){
                    checkWinner();
                } else {
                    startWaitingForOpponentClickedButtonNumber();
                }
                buttonsEnabled = false;
                toggleButtons();
            });    
        }
    }
    
    private void toggleButtons(){
        for(int i = 0; i < this.buttons.length; ++i){
            if(!alreadyClickedButtons[i])
                this.buttons[i].setEnabled(buttonsEnabled);
            else
                this.buttons[i].setEnabled(false);
        }
    }
    
    public void startWaitingForOpponentClickedButtonNumber(){
        Thread thread = new Thread(() -> {
            while(true){
                updateTurn();
            }
        });
        thread.start();
    }
    
    public void updateTurn(){
        int btnNum = clientSideConnection.receiveButtonNumber();
        messagesArea.setText("Your opponent clicked button #" + btnNum
                + ". Your turn.");
        opponentPoints += this.values[btnNum - 1];
        if(id == 1 && turnsMade == maxTurns){
            checkWinner();
        }
        System.out.println("You enemy has " + opponentPoints + ".");
        buttonsEnabled = true;
        toggleButtons();
    }
    
    public void checkWinner(){
        if(myPoints > opponentPoints){
                messagesArea.setBackground(Color.green);
                messagesArea.setText("You won!!!\nYOU: " + myPoints
                        + "\nOPPONENT: " + opponentPoints);
            } else if(myPoints < opponentPoints){
                messagesArea.setBackground(Color.red);
                messagesArea.setText("You lost!!!\nYOU: " + myPoints
                        + "\nOPPONENT: " + opponentPoints);
            } else {
                messagesArea.setBackground(Color.cyan);
                messagesArea.setText("It's tie!!!\nYou both have: " + myPoints);
        }
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
                maxTurns = this.dis.readInt() >> 1;
                System.out.println("maxTurns = " + maxTurns);
                turnsMade = this.dis.readInt();
                System.out.println("turnsMade = " + turnsMade);
                for(int i = 0; i < 4; ++i){
                    values[i] = this.dis.readInt();
                    System.out.println("value #" + (i + 1) + " is " + values[i]);
                }
                
            } catch (IOException ex) {
                Logger.getLogger(ClientSideConnection.class.getName())
                        .log(Level.SEVERE, "IOException from"
                                + " ClientSideConnection constructor", ex);
            }
        }
        
        public void sendButtonNumber(int buttonNumber){
            try {
                this.dos.writeInt(buttonNumber);
                this.dos.flush();
            } catch (IOException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        
        public int receiveButtonNumber(){
            int btnNum = -1;
            try {
                btnNum = this.dis.readInt();                
            } catch (IOException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            return btnNum;
        }
    }
    
    public static void main(String[] args) {
        Player player = new Player(500, 100);
        player.connectToServer("127.0.0.1", 55555);
        player.setupButtons();
        player.setupGUI();
    }
}
