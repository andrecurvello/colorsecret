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
 */
package org.jraf.android.slavebody.activity;

import org.jraf.android.slavebody.Constants;
import org.jraf.android.slavebody.R;
import org.jraf.android.slavebody.model.CodePeg;
import org.jraf.android.slavebody.model.Game;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private static final int DIALOG_CHOOSE_PEG = 0;

    private Game mGame;

    private ViewGroup mRootView;
    private LayoutInflater mLayoutInflater;

    protected int mSelectingRowIndex;
    protected int mSelectingPegIndex;
    protected View mSelectingPeg;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGame = new Game(Constants.DEFAULT_NB_HOLES, Constants.DEFAULT_NB_ROWS);

        mLayoutInflater = LayoutInflater.from(this);
        mRootView = (ViewGroup) findViewById(R.id.root);
        createRows(Constants.DEFAULT_NB_HOLES, Constants.DEFAULT_NB_ROWS);
        setActiveRow(0);
    }

    private void createRows(final int nbHoles, final int nbRows) {
        for (int i = 0; i < nbRows; i++) {
            final View row = createRow(nbHoles, i);
            mRootView.addView(row);
        }
    }

    private View createRow(final int nbHoles, final int rowIndex) {
        final LinearLayout res = (LinearLayout) mLayoutInflater.inflate(R.layout.row, null, false);
        for (int i = 0; i < nbHoles; i++) {
            final View peg = mLayoutInflater.inflate(R.layout.peg, res, false);
            peg.setClickable(true);
            final int selectingPegIndex = i;
            peg.setOnClickListener(new OnClickListener() {
                public void onClick(final View v) {
                    mSelectingRowIndex = rowIndex;
                    mSelectingPegIndex = selectingPegIndex;
                    mSelectingPeg = peg;
                    showDialog(DIALOG_CHOOSE_PEG);
                }
            });
            res.addView(peg);
        }
        res.addView(createHintPegs(nbHoles));
        return res;
    }

    private View createHintPegs(final int nbHoles) {
        final LinearLayout res = (LinearLayout) mLayoutInflater.inflate(R.layout.hints, null, false);
        for (int i = 0; i < nbHoles; i++) {
            final ImageView peg = (ImageView) mLayoutInflater.inflate(R.layout.peg, res, false);
            peg.setImageResource(R.drawable.peg_hint_empty);
            res.addView(peg);
        }
        return res;
    }

    private View createOkButton() {
        final LinearLayout res = (LinearLayout) mLayoutInflater.inflate(R.layout.hints, null, false);
        res.addView(mLayoutInflater.inflate(R.layout.button_ok, null, false));

        return res;
    }

    private void setActiveRow(final int rowIndex) {
        final ViewGroup row = (ViewGroup) mRootView.getChildAt(rowIndex);
        row.setSelected(true);
        final int childCount = row.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            row.getChildAt(i).setFocusable(true);
        }
        // remove the hint pegs which is the last child of the row
        row.removeViewAt(childCount - 1);

        // replace it with the OK button
        row.addView(createOkButton());
    }

    private void setCodePeg(final int rowIndex, final int holeIndex, final CodePeg codePeg) {}

    /*
     * Dialog.
     */
    @Override
    protected Dialog onCreateDialog(final int id) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_CHOOSE_PEG:
                builder.setTitle(R.string.dialog_choosePeg_title);
                builder.setSingleChoiceItems(new PegListAdapter(this), -1, mChoosePegOnClickListener);
                builder.setNegativeButton(android.R.string.cancel, null);
            break;
        }
        return builder.create();
    }

    private final DialogInterface.OnClickListener mChoosePegOnClickListener = new DialogInterface.OnClickListener() {

        public void onClick(final DialogInterface dialog, final int which) {
            dialog.dismiss();
        }
    };
}