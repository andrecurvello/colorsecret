/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2011 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.colorsecret.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jraf.android.colorsecret.Constants;
import org.jraf.android.colorsecret.model.Board.Row;

import android.util.Log;

/**
 * Represents an ongoing game.
 */
public class Game {
    private static final String TAG = Constants.TAG + Game.class.getSimpleName();

    public static enum GuessResult {
        TRY_AGAIN, GAME_OVER, YOU_WON,
    }

    private final int mNbHoles;
    private final int mNbRows;

    private final Board mBoard;

    private int mCurrentGuess = 0;

    /**
     * @param nbHoles
     *            number of holes for each row.
     * @param nbRows
     *            total number of rows.
     */
    public Game(final int nbHoles, final int nbRows) {
        mNbHoles = nbHoles;
        mNbRows = nbRows;
        mBoard = new Board(nbHoles, nbRows);
    }

    public Board getBoard() {
        return mBoard;
    }

    public void setSecret(final CodePeg... codePegs) {
        mBoard.getSecretRow().setCodePegs(codePegs);
    }

    public void setRandomSecret() {
        final List<CodePeg> secret = new ArrayList<CodePeg>(mNbHoles);
        final Random random = new Random();
        final CodePeg[] values = CodePeg.values();
        for (int i = 0; i < mNbHoles; i++) {
            secret.add(values[random.nextInt(values.length)]);
        }
        if (Constants.LOGD) Log.d(TAG, "Secret: " + secret);
        mBoard.getSecretRow().setCodePegs(secret.toArray(new CodePeg[mNbHoles]));
    }

    public CodePeg[] getSecret() {
        return mBoard.getSecretRow().getCodePegs();
    }

    public void setGuess(final int rowIndex, final int holeIndex, final CodePeg codePeg) {
        if (rowIndex >= mNbRows) {
            throw new IndexOutOfBoundsException("rowIndex >= mNbRows");
        }
        if (holeIndex >= mNbHoles) {
            throw new IndexOutOfBoundsException("holeIndex >= mNbHoles");
        }
        mBoard.getGuessRows()[rowIndex].setCodePeg(holeIndex, codePeg);
    }

    public boolean isRowComplete(final int rowIndex) {
        final Row row = mBoard.getGuessRows()[rowIndex];
        for (int i = 0; i < mNbHoles; i++) {
            if (row.getCodePegs()[i] == null) {
                return false;
            }
        }
        return true;
    }

    public GuessResult validateGuess() {
        if (mCurrentGuess == mNbRows) {
            throw new IndexOutOfBoundsException("Already reached the maximum number of guesses");
        }
        computeHints();
        final List<HintPeg> hintPegs = mBoard.getGuessRows()[mCurrentGuess].getHintPegs();
        int i = 0;
        for (final HintPeg hintPeg : hintPegs) {
            if (hintPeg != HintPeg.COLOR_AND_POSITION) {
                break;
            }
            i++;
        }
        if (i == mNbHoles) {
            // we have nbHoles color+position pegs, that means all of them are correct: we won
            return GuessResult.YOU_WON;
        }
        mCurrentGuess++;
        if (mCurrentGuess == mNbRows) {
            return GuessResult.GAME_OVER;
        }
        return GuessResult.TRY_AGAIN;
    }

    /**
     * Compute the hint pegs for the current guess and add them to the board.
     */
    private void computeHints() {
        final Row guessRow = mBoard.getGuessRows()[mCurrentGuess];
        final CodePeg[] guessPegs = guessRow.getCodePegs();
        final Row secretRow = mBoard.getSecretRow();
        final CodePeg[] secretPegs = secretRow.getCodePegs();

        // look for correct color+positions first
        for (int idx = 0; idx < mNbHoles; idx++) {
            final CodePeg guessPeg = guessPegs[idx];
            final CodePeg secretPeg = secretPegs[idx];
            if (secretPeg == guessPeg) {
                guessRow.addHintPeg(HintPeg.COLOR_AND_POSITION);
                // now that we found it, remove this guess peg from further comparisons
                secretPegs[idx] = null;
                guessPegs[idx] = null;
            }
        }

        // now look for correct color only
        // iterate over guess pegs
        for (int guessIdx = 0; guessIdx < mNbHoles; guessIdx++) {
            final CodePeg guessPeg = guessPegs[guessIdx];
            if (guessPeg == null) {
                //already found
                continue;
            }

            // look for this guess peg in the secret pegs
            for (int secretIdx = 0; secretIdx < mNbHoles; secretIdx++) {
                final CodePeg secretPeg = secretPegs[secretIdx];

                if (secretPeg == guessPeg) {
                    // we found one!
                    guessRow.addHintPeg(HintPeg.COLOR_ONLY);
                    // now that we found it, remove this secret peg from further comparisons
                    guessPegs[guessIdx] = null;
                    secretPegs[secretIdx] = null;
                    break;
                }
            }
        }

    }

    public List<HintPeg> getHints(final int rowIndex) {
        return mBoard.getGuessRows()[rowIndex].getHintPegs();
    }

    public int getCurrentGuess() {
        return mCurrentGuess;
    }
}
