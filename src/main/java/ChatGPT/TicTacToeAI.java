package ChatGPT;

import java.util.*;

public class TicTacToeAI {

    static final char EMPTY = '.';
    static final char X = 'X';
    static final char O = 'O';

    // Field values (3 2 3 / 2 4 2 / 3 2 3)
    static final int[] FIELD_VALUE = {
            3, 2, 3,
            2, 4, 2,
            3, 2, 3
    };

    static final int WIN_SCORE = 10_000; // big number for terminal wins

    // All winning lines (triples of indices)
    static final int[][] LINES = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
    };

    static class MoveResult {
        int move;   // index 0..8
        int score;  // minimax score
        MoveResult(int move, int score) { this.move = move; this.score = score; }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        char[] board = new char[9];
        Arrays.fill(board, EMPTY);

        System.out.print("Choose your player (X/O): ");
        char human = Character.toUpperCase(sc.nextLine().trim().charAt(0));
        if (human != X && human != O) human = X;
        char ai = (human == X) ? O : X;

        System.out.print("Choose search depth (1-9): ");
        int depth = parseIntSafe(sc.nextLine(), 9);
        depth = Math.max(1, Math.min(9, depth));

        System.out.print("Use alpha-beta pruning? (y/n): ");
        boolean useAB = sc.nextLine().trim().equalsIgnoreCase("y");

        // X starts in TicTacToe by convention
        char current = X;

        while (true) {
            printBoard(board);

            char w = winner(board);
            if (w != EMPTY) {
                System.out.println("Winner: " + w);
                break;
            }
            if (isFull(board)) {
                System.out.println("Draw!");
                break;
            }

            if (current == human) {
                int move = askHumanMove(sc, board);
                board[move] = human;
            } else {
                System.out.println("AI thinking...");
                int move = bestMove(board, ai, depth, useAB);
                board[move] = ai;
                System.out.println("AI plays: " + (move + 1));
            }

            current = (current == X) ? O : X;
        }
    }

    static int bestMove(char[] board, char aiPlayer, int depth, boolean useAB) {
        MoveResult res;
        if (useAB) {
            res = alphabetaRoot(board, aiPlayer, depth);
        } else {
            res = minimaxRoot(board, aiPlayer, depth);
        }
        return res.move;
    }

    // ---------- ROOT SEARCH (chooses move) ----------

    static MoveResult minimaxRoot(char[] board, char aiPlayer, int depth) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int move : legalMoves(board)) {
            board[move] = aiPlayer;
            int score = minimax(board, depth - 1, opposite(aiPlayer), aiPlayer);
            board[move] = EMPTY;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return new MoveResult(bestMove, bestScore);
    }

    static MoveResult alphabetaRoot(char[] board, char aiPlayer, int depth) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int move : legalMoves(board)) {
            board[move] = aiPlayer;
            int score = alphabeta(board, depth - 1, opposite(aiPlayer), aiPlayer, alpha, beta);
            board[move] = EMPTY;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return new MoveResult(bestMove, bestScore);
    }

    // ---------- MINIMAX ----------

    static int minimax(char[] board, int depthLeft, char currentPlayer, char maximizingPlayer) {
        char w = winner(board);
        if (w != EMPTY) {
            // If winner is maximizingPlayer: positive, else negative
            return (w == maximizingPlayer) ? WIN_SCORE : -WIN_SCORE;
        }
        if (isFull(board)) return 0;
        if (depthLeft == 0) return evaluate(board, maximizingPlayer);

        boolean isMax = (currentPlayer == maximizingPlayer);

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int move : legalMoves(board)) {
            board[move] = currentPlayer;
            int score = minimax(board, depthLeft - 1, opposite(currentPlayer), maximizingPlayer);
            board[move] = EMPTY;

            if (isMax) best = Math.max(best, score);
            else best = Math.min(best, score);
        }
        return best;
    }

    // ---------- ALPHA-BETA ----------

    static int alphabeta(char[] board, int depthLeft, char currentPlayer, char maximizingPlayer, int alpha, int beta) {
        char w = winner(board);
        if (w != EMPTY) {
            return (w == maximizingPlayer) ? WIN_SCORE : -WIN_SCORE;
        }
        if (isFull(board)) return 0;
        if (depthLeft == 0) return evaluate(board, maximizingPlayer);

        boolean isMax = (currentPlayer == maximizingPlayer);

        if (isMax) {
            int value = Integer.MIN_VALUE;
            for (int move : legalMoves(board)) {
                board[move] = currentPlayer;
                value = Math.max(value, alphabeta(board, depthLeft - 1, opposite(currentPlayer), maximizingPlayer, alpha, beta));
                board[move] = EMPTY;

                alpha = Math.max(alpha, value);
                if (alpha >= beta) break; // beta cutoff
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (int move : legalMoves(board)) {
                board[move] = currentPlayer;
                value = Math.min(value, alphabeta(board, depthLeft - 1, opposite(currentPlayer), maximizingPlayer, alpha, beta));
                board[move] = EMPTY;

                beta = Math.min(beta, value);
                if (beta <= alpha) break; // alpha cutoff
            }
            return value;
        }
    }

    // ---------- EVALUATION ----------

    static int evaluate(char[] board, char maximizingPlayer) {
        char minPlayer = opposite(maximizingPlayer);

        // 1) Positional score (field values)
        int score = 0;
        for (int i = 0; i < 9; i++) {
            if (board[i] == maximizingPlayer) score += FIELD_VALUE[i];
            else if (board[i] == minPlayer) score -= FIELD_VALUE[i];
        }

        // 2) Line potential (simple heuristic)
        // +50 if you have 2-in-line and 1 empty, -50 if opponent does
        for (int[] line : LINES) {
            int maxCount = 0, minCount = 0, emptyCount = 0;
            for (int idx : line) {
                if (board[idx] == maximizingPlayer) maxCount++;
                else if (board[idx] == minPlayer) minCount++;
                else emptyCount++;
            }

            if (maxCount == 2 && emptyCount == 1) score += 50;
            if (minCount == 2 && emptyCount == 1) score -= 50;

            // Optional: small bonus for 1-in-line
            if (maxCount == 1 && emptyCount == 2) score += 5;
            if (minCount == 1 && emptyCount == 2) score -= 5;
        }

        return score;
    }

    // ---------- HELPERS ----------

    static List<Integer> legalMoves(char[] board) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (board[i] == EMPTY) moves.add(i);
        return moves;
    }

    static char winner(char[] b) {
        for (int[] line : LINES) {
            char a = b[line[0]], c = b[line[1]], d = b[line[2]];
            if (a != EMPTY && a == c && c == d) return a;
        }
        return EMPTY;
    }

    static boolean isFull(char[] b) {
        for (char c : b) if (c == EMPTY) return false;
        return true;
    }

    static char opposite(char p) {
        return (p == X) ? O : X;
    }

    static int askHumanMove(Scanner sc, char[] board) {
        while (true) {
            System.out.print("Your move (1-9): ");
            String s = sc.nextLine().trim();
            int move = parseIntSafe(s, -1) - 1;
            if (move >= 0 && move < 9 && board[move] == EMPTY) return move;
            System.out.println("Invalid move. Try again.");
        }
    }

    static int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }

    static void printBoard(char[] b) {
        System.out.println();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                char ch = b[r * 3 + c];
                System.out.print(" " + (ch == EMPTY ? (r * 3 + c + 1) : ch) + " ");
                if (c < 2) System.out.print("|");
            }
            System.out.println();
            if (r < 2) System.out.println("---+---+---");
        }
        System.out.println();
    }
}

