package egenTIcTacToc;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {

    // The Broad
    public static char[][] board = new char[3][3];

    // Variables for X, O and Empty
    static final char x = 'X';
    static final char o = 'O';
    static final char empty = ' ';

    // Weight (number of winner-lines)
    static final int[][] WEIGHTS = {
            {3, 2, 3},
            {2, 4, 2},
            {3, 2, 3}
    };

    public static void main(String[] args) {
        // User Input with scanner
        Scanner sc = new Scanner(System.in);
        char choice;
        int maxDepth;

        //Choices (x or o)
        while (true) {
            System.out.println("Choose X or O:");
            String s = sc.nextLine().trim().toUpperCase();

            if (s.equals("X") || s.equals("O")) {
                choice = s.charAt(0);
                break;
            }
            System.out.println("Invalid input. Try Again.");
        }
        // difficulty / depth
        while (true) {
            System.out.println("Choose AI search depth (1-9):");
            String s = sc.nextLine().trim();
            try {
                maxDepth = Integer.parseInt(s);
                if (maxDepth < 1 || maxDepth > 9) {
                    System.out.println("Depth must be 1-9");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number");
            }
        }

        int algorithmChoice;
        while (true) {
            System.out.println("Chooser AI algorithm:");
            System.out.println("1 = MiniMax");
            System.out.println("2 = Alpha-Beta Pruning");
            String s = sc.nextLine().trim();
            try {
                algorithmChoice = Integer.parseInt(s);
                if (algorithmChoice != 1 && algorithmChoice != 2) {
                    System.out.println("Invalid input. Choose 1 or 2 only");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number");
            }
        }

        boolean useAlphaBeta = (algorithmChoice == 2);

        System.out.println("You chose " + choice + ", depth " + maxDepth +
                ", algorithm: " + (useAlphaBeta ? "Alpha-Beta" : "MiniMax"));
        //_________________________________

        // AI and Human variables
        final char humanSymbol = choice;
        final char aiSymbol = (humanSymbol == x) ? o : x;
        char currentPlayer = x; // X starter typisk
        // ________________________________


        initBoard();
        boardOutput();

        // The game between Human and AI till winner or draw
        while (true) {
            if (currentPlayer == humanSymbol) {
                System.out.println("Choose number to mark (1-9):");
                String input = sc.nextLine().trim();

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
                System.out.println("AI is thinking (" + (useAlphaBeta ? "Alpha-Beta" : "Minimax") + "):");

                Move best;
                if (useAlphaBeta) {
                   best = findBestMoveAlphaBeta(maxDepth, humanSymbol, aiSymbol);
                } else {
                    best = findBestMoveMinimax(maxDepth, humanSymbol, aiSymbol);
                }
                if (best != null) {
                    makeMove(best, currentPlayer); // current Player is AI here.
                }
            }

            // Print board ét gang pr. tur.
            boardOutput();

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

    // AI Available moves
    public static class Move {
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

    // Evalutates__________________________
    public static Integer evaluateTerminal(char human, char ai) {
        if (checkWin(ai)) return 10; // AI vinder hurtigere = bedre
        if (checkWin(human)) return -10; // Human vinder hurtigere = værre
        if (isBoardFull()) return 0; // Draw
        return null; // ikke terminal.
    }

    public static int evaluateBoard(char human, char ai) {
        int score = 0;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == ai) {
                    score += WEIGHTS[r][c];
                } else if (board[r][c] == human) {
                    score -= WEIGHTS[r][c];
                }
            }
        }

        return score;
    }
    //_____________________________________

    // Conditions _________________________
    // Winning for both AI and Human_______
    public static boolean checkWin(char p) {
        // Note: [row][col]
        // Rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0] == p && board[r][1] == p && board[r][2] == p) return true;
        }

        // Cols
        for (int c = 0; c < 3; c++) {
            if (board[0][c] == p && board[1][c] == p && board[2][c] == p) return true;
        }

        // Diagonals
        if (
                (board[0][0] == p && board[1][1] == p && board[2][2] == p) ||
                (board[0][2] == p && board[1][1] == p && board[2][0] == p)
        ) return true;

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

    // ___________Minimax__________________
    public static int minimax(int depth, boolean isMaximizing, char human, char ai) {

        Integer terminal = evaluateTerminal(human, ai); // Wrapper-class på et objekt der indeholder et tal
        if (terminal != null) return terminal;

        // max search depth
        if (depth == 0) return evaluateBoard(human, ai); // midlertidigt: "Ingen viden"

        List<Move> moves = getAvailableMoves();

        //MinMax udregning (hovedpunktet)
        if (isMaximizing) {
            //MIN
            int bestScore = Integer.MIN_VALUE;

            for (Move m : moves) {
                makeMove(m, ai);
                int score = minimax(depth - 1, false, human, ai);
                undoMove(m);

                if (score > bestScore) bestScore = score;
            }
            return bestScore;

        } else {
            //MAX
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
    public static Move findBestMoveMinimax(int depth, char human, char ai) {
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

    // _________Alpha-Beta Pruning_________
    public static int alphabeta(int depth, boolean isMaximizing, char human, char ai, int alpha, int beta) {

        Integer terminal = evaluateTerminal(human, ai);
        if (terminal != null) return terminal;

        if (depth == 0) return evaluateBoard(human, ai);

        List<Move> moves = getAvailableMoves();

        // ALPHABETA Pruning udregning (hovedpunktet)
        if (isMaximizing) {
            //MIN
            int bestScore = Integer.MIN_VALUE;

            for (Move m : moves) {
                makeMove(m, ai);
                int score = alphabeta(depth - 1, true, human, ai, alpha, beta);
                undoMove(m);

                if (score > bestScore) bestScore = score;
                if (bestScore > alpha) alpha = bestScore;

                if (alpha >= beta) break; // PRUNE
            }
            return bestScore;
        } else {
            //MAX
            int bestScore = Integer.MAX_VALUE;

            for (Move m : moves) {
                makeMove(m, human);
                int score = alphabeta(depth - 1, true, human, ai, alpha, beta);
                undoMove(m);

                if (score < bestScore) bestScore = score;
                if (bestScore < beta) beta = bestScore;

                if (alpha >= beta) break; // PRUNE
            }
            return bestScore;
        }
    }

    public static Move findBestMoveAlphaBeta(int depth, char human, char ai) {
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move m : getAvailableMoves()) {
            makeMove(m, ai);
            int score = alphabeta(depth - 1, true, human, ai, alpha, beta);
            undoMove(m);

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }

            if (bestScore > alpha) alpha = bestScore; // root update helps prune
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

// MiniMax undersøger alt, også de dårlige.
// Alpha/Beta Pruning gør præcis det samme som Minimax,
// MEN det stopper med at undersøg grene, som den allerede ved er ligegyldige.
// Den "klipper" (prunes) dele af træet væk (de dårlige muligheder)

