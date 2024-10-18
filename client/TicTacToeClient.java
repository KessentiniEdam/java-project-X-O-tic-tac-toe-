package client;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TicTacToeClient {

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("");
    String s;
    String so;

    private String x="X" ;
    private String o="O" ;


    private Square[] board = new Square[9];
    private Square currentSquare;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Constructs the client by connecting to a server, laying out the GUI and registering GUI listeners.

    public TicTacToeClient() throws Exception {

        // Setup networking
        socket = new Socket("127.0.0.1", 999);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Layout GUI
        messageLabel.setBackground(Color.lightGray);
        frame.add(messageLabel, "South");

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
           int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                 currentSquare = board[j];
                    //currentSquare = board[i];
                    out.println("MOVE " + j);}});
                 //   out.println("MOVE " + i);}});
            boardPanel.add(board[i]);
        }
        frame.add(boardPanel, "Center");
    }
    public void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                s = (mark == 'X' ? x : o);
                so = (mark == 'X' ? o : x);
                frame.setTitle("Tic Tac Toe - Player " + mark);
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setText(s);

                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(so);

                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }

        }
        finally {
            socket.close();
        }
    }



    static class Square extends JPanel {
        JLabel label = new JLabel();

        public Square() {
            setBackground(Color.white);
            add(label);
        }

        public void setText(String s) {
            label.setText(s);
        }
    }


    //main
    public static void main(String[] args) throws Exception {

            TicTacToeClient client = new TicTacToeClient();
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(500, 300);
            client.frame.setVisible(true);
            client.play();


    }
}