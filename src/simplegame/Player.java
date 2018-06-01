package simplegame;

import java.awt.Container;
import java.awt.GridLayout;
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
    
    public Player(int width, int height){
        this.width = width;
        this.height = height;
        this.frame = new JFrame();
        for(int i = 0; i < 4; ++i){
            this.buttons[i] = new JButton(String.valueOf(i + 1));
        }
        this.messagesArea = new JTextArea();
        this.container = this.frame.getContentPane();
    }
    
    public void setupGUI(){
        this.frame.setSize(width, height);
        this.frame.setTitle(WINDOW_TITLE);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.container.setLayout(new GridLayout(1, 5));
        this.container.add(this.messagesArea);
        
        for(JButton btn: buttons){
            this.container.add(btn);
        }
    }
}
