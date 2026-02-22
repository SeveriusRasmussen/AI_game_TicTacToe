package group;
import java.util.*;

public class Main {

    // The Broad
    public static char[][] board = new char[3][3];

    // Variables for X, O and Empty
    static final char x = 'X';
    static final char o = 'O';
    static final char empty = ' ';

    // Weight
    static final int[][] WEIGHTS = {
            {3, 2, 3},
            {2, 4, 2},
            {3, 2, 3}
    };

    public static void main(String[] args) {
        // User Input with scanner
        Scanner sc = new Scanner(System.in);
        String choice;
        char finalChoice;

        //Choice of X or O
        while (true) {
            System.out.println("Choose X or O:");

            // trim() removes spaces from etc. " x" to "x".
            choice = sc.nextLine().trim().toUpperCase();

            if (choice.equals("X") || choice.equals("O")) {
                finalChoice = choice.charAt(0); //Takes first letter
                break;
            }

            System.out.println("Invalid input. Try Again.");
        }
        System.out.println("You chose " + choice);
        //_________________________________

        // AI and Human variables
        char human = finalChoice;
        char ai = (human == x) ? o : x;
        char currentPlayer = x; // X starter typisk og currentPlayer styrer ALT
        // ________________________________


        initBoard();
        boardOutput();

        // mark for the player
        while (true) {
            if (currentPlayer == human) {
                System.out.println("Choose number to mark (1-9):");
                String input = sc.nextLine().trim().toUpperCase();

                int move;

                // The input should be an integer
                try {
                    move = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number. Try Again.");
                    continue;
                }

                // The input should be between 1 and 9
                if (move < 1 || move > 9) {
                    System.out.println("Number must be between 1 and 9.");
                    continue;
                }

                int row = (move -1) / 3;
                int col = (move -1) % 3;
            /* DANSK
            Med / (Heltalsdivision), når man vælger 1, så 1 - 1 = 0 derefter 0 / 3 = 0.
            Hvis 4, så får man 3 / 3 = 1. 6 / 3 = 2.
            Med % (rest "modulus"), når man vælger 1, så 1 - 1 = 0 derefter 0 % 3 = 0.
            Hvis 4 så trækker 3 fra og får 1. Med 8 trækker man to gange og får 2.

            samlet: Hvis man vælger 8, så 8 - 1 = 7 -> 7 / 3 (row) = 2 og 7 % 3 = 1 (col)
            Så har man board[2][1] som resultat!
            */

                // if the spot is taken
                if (board[row][col] != empty) {
                    System.out.println("That spot is already marked.");
                    continue;
                }

                // if the above methods is ok, mark that chosen spot.
                board[row][col] = currentPlayer;
            } else {
                // AI Turn
                System.out.println("Available moves: " + getAvailableMoves().size());
                System.out.println("AI is thinking:");
                int depth = 9; // should be changed by human's choice.
                Move best = findBestMove(depth, human, ai);
                if (best != null) {
                    makeMove(best, currentPlayer); // current Player is AI here.
                }
            }

            // Print board ét gang pr. tur.
            boardOutput();

            /* Terminal test
            Integer t = evaluateTerminal(human, ai);
            if (t != null) {
                System.out.println("Terminal score = " + t);
            }
            */

            // Win/draw check
            if (checkWin(currentPlayer)) {
                System.out.println(currentPlayer + " won! Thanks for playing!");
                boardOutput();
                break;
            };

            if (isBoardFull()) {
                System.out.println("It's a draw!");
                boardOutput();
                break;
            }

            // Switch player
            currentPlayer = (currentPlayer == x) ? o : x;
        }


    }

    // Random PC play style _______________
    static final Random rnd = new Random();

    public static void aiMoveRandom(char player) {
        while (true) {
            int move = rnd.nextInt(9) + 1; // 1..9
            int row = (move - 1) / 3;
            int col = (move - 1) % 3;

            if (board[row][col] == empty) {
                board[row][col] = player;
                break;
            }
        }
    }

    // AI Available moves
    static class Move {
        int row, col;
        Move(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static List<Move> getAvailableMoves() {
        List<Move> moves = new ArrayList<>();
        for ( int r = 0; r < 3; r++ ) {
            for ( int c = 0; c < 3; c++ ) {
                if (board[r][c] == empty) {
                    moves.add(new Move(r, c));
                }
            }
        }
        return moves;
    }

    public static void makeMove(Move m, char p) {
        board[m.row][m.col] = p;
    }

    public static void undoMove(Move m) {
        board[m.row][m.col] = empty;
    }

    public static Integer evaluateTerminal(char human, char ai, int depth) {
        if (checkWin(ai)) return 10 - depth; // AI vinder hurtigere = bedre
        if (checkWin(human)) return -10 + depth; // Human vinder hurtigere = værre
        if (isBoardFull()) return 0; // Draw
        return null; // ikke terminal.
    }
    //_____________________________________

    // Conditions _________________________
    // Winning for both AI and Human_______
    public static boolean checkWin(char p) {
        // Rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0] == p && board[r][1] == p && board[r][2] == p) return true;
        }

        // Cols
        for (int c = 0; c < 3; c++) {
            if (board[0][c] == p && board[1][c] == p && board[2][c] == p) return true;
        }

        // Diagonals
        if (board[0][0] == p && board[1][1] == p && board[2][2] == p) return true;
        if (board[0][2] == p && board[1][1] == p && board[2][0] == p) return true;

        return false;
    }

    // If the board is a draw
        public static boolean isBoardFull() {
            for (char[] chars : board) {
                for (char aChar : chars) {
                    if (aChar == empty) return false;
                }
            }
            return true;
        }
    //_____________________________________

    // Minimax_____________________________
    public static int minimax(int depth, boolean isMaximizing, char human, char ai) {

        Integer terminal = evaluateTerminal(human, ai, depth); // Wrapper-class på et objekt der indeholder et tal
        if (terminal != null) return terminal;

        // max search depth
        if (depth == 0) return 0; // midlertidigt: "Ingen viden"

        List<Move> moves = getAvailableMoves();

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;

            for (Move m : moves) {
                makeMove(m, ai);
                int score = minimax(depth - 1, false, human, ai);
                undoMove(m);

                if (score > bestScore) bestScore = score;
            }
            return bestScore;

        } else {
            int bestScore = Integer.MAX_VALUE;

            for (Move m : moves) {
                makeMove(m, human);
                int score = minimax(depth - 1, true, human, ai);
                undoMove(m);

                if (score < bestScore) bestScore = score;
            }
            return bestScore;
        }
    }

    // Find Best Move
    public static Move findBestMove(int depth, char human, char ai) {
        int bestScore=Integer.MIN_VALUE;
        Move bestMove = null;

        for (Move m : getAvailableMoves()) {
            makeMove(m, ai);
            int score = minimax(depth - 1, false, human, ai);
            undoMove(m);

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }

        return bestMove;
    }
    //_____________________________________




    // Output of broad ____________________
    // Start state: Fill the broad with the empty-variabel
    public static void initBoard() {
        // Rækker forloop
        for (int i = 0; i < board.length; i++) {
            // Kolonner forloop
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = empty;
            }
        }
    }

    // Output of the broad with 1-9 colorful
    public static void boardOutput() {
        String RED = " \u001B[31m";
        String GREEN = " \u001B[32m";
        String BLUE = " \u001B[34m";
        String RESET = "\u001B[0m |";
        int counter = 1;

        for (char[] chars : board) {

            System.out.print("|");

            for (char aChar : chars) {

                if (aChar == empty) {
                    System.out.print(GREEN + counter + RESET);
                } else if (aChar == x) {
                    System.out.print(RED + "X" + RESET);
                } else if (aChar == o) {
                    System.out.print(BLUE + "O" + RESET);
                }

                counter++;
            }
            System.out.println();
            System.out.println("*___*___*___*");
        }
    }

    //_____________________________________
}
