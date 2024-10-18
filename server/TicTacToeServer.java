package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
public class TicTacToeServer {

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(999);
        System.out.println("Serveur en cours");
        try {

                Game game = new Game();
                Game.Player playerX = game.new Player(server.accept(), 'X');
                Game.Player playerO = game.new Player(server.accept(), 'O');
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX);
                game.currentPlayer = playerX;
                playerX.start();
                playerO.start();

        } finally {
            server.close();
        }
    }
}

class Game {

    //  board (tableau) de 9 cases
    private Player[] board = {
            null, null, null,
            null, null, null,
            null, null, null};

    //current player
    Player currentPlayer;



    // winner
    public boolean hasWinner() {
        return
                (board[0] != null && board[0] == board[1] && board[0] == board[2])
                        ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
                        ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
                        ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
                        ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
                        ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
                        ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
                        ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    // be sure que les carreaux sont tous remplis
    public boolean boardFilledUp() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }
    // thread quand un joueur joue son role , il faut etre synchroniser pour que l'aurtre joueur ne peut pas play
    public synchronized boolean legalMove(int location, Player player) {
        if (player == currentPlayer && board[location] == null) {
            //reserver le carreaux pour celui qui tape
            board[location] = currentPlayer;
            // puis c'est le tour de l'aurre opponent adversaire
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(location);
            return true;
        }
        return false;
    }
    // class player : elle appartient à la classe mère "game" avec une relation de composition

    class Player extends Thread {
        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        // initialiser le constructeur avec x ou o , et ouvrir les socket en mode lecture et ecriture
        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " );
            }
        }
        //determiner l'opponent
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }




        //envoyer l'emplacement de clic vers l'adversaire
        public synchronized void  otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
            // si le jeu est terminé avec votre échec
            output.println(
                    // kenou m3ebbi tet3adlou
                    hasWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
        }

        public void run() {
            try {
                //  thread start lors que 2 joueurs ready
                output.println("MESSAGE All players connected");

                // envoyer au joueur x qu'il est son role
                if (mark == 'X') {
                    output.println("MESSAGE ton tour");
                }
                else if (mark=='O') {                    output.println("MESSAGE le tour de  x");
                }


                // toujours attendre les commandes des joueurs
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (legalMove(location, this)) {
                            output.println("VALID_MOVE");
                            output.println(hasWinner() ? "VICTORY"
                                    : boardFilledUp() ? "TIE"
                                    : "");
                        } else {
                            output.println("MESSAGE ?");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(" un des joueurs est déconnecté: " + e);
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
}