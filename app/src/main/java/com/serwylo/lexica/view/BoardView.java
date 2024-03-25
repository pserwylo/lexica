/*
 *  Copyright (C) 2008-2009 Rev. Johnny Healey <rev.null@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serwylo.lexica.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.serwylo.lexica.R;
import com.serwylo.lexica.db.GameMode;
import com.serwylo.lexica.game.Game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BoardView extends View {

    @SuppressWarnings("unused")
    protected static final String TAG = "LexicaView";

    private Game game;

    private final ThemeProperties theme;

    private int width;
    private int height;
    private int gridsize;
    private float boxsize;
    private int boardWidth;
    private final Paint p;
    private Set<Integer> highlightedPositions = new HashSet<>();
    private int maxWeight;

    public BoardView(Context context) {
        this(context, null);
    }

    public BoardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.lexicaViewStyle);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        theme = new ThemeProperties(context, attrs, defStyle);

        p = new Paint();
        p.setTextAlign(Paint.Align.CENTER);
        p.setAntiAlias(true);
        p.setStrokeWidth(2);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        theme = new ThemeProperties(context, attrs, defStyleAttr);

        p = new Paint();
        p.setTextAlign(Paint.Align.CENTER);
        p.setAntiAlias(true);
        p.setStrokeWidth(2);
    }

    public void setGame(Game game) {
        this.game = game;

        maxWeight = game.getMaxWeight(); // Don't calculate this on each paint for performance.
        boardWidth = game.getLetterGrid().getWidth();
    }

    public void highlight(Integer[] highlightedPositions) {
        this.highlightedPositions = new HashSet<>();
        this.highlightedPositions.addAll(Arrays.asList(highlightedPositions));
    }

    private void setDimensions(int w, int h) {
        width = w;
        height = h;

        gridsize = Math.min(width, height);
        boxsize = ((float) gridsize) / boardWidth;
    }

    private final Rect textBounds = new Rect();

    private void drawBoard(Canvas canvas) {
        if (game == null) {
            return;
        }

        // Draw boxes
        for (int i = 0; i < game.getLetterGrid().getSize(); i++) {
            int pos = game.getLetterGrid().getRotatedPosition(i);

            int x = i % game.getLetterGrid().getWidth();
            int y = i / game.getLetterGrid().getWidth();

            if (highlightedPositions.contains(i)) {
                p.setColor(theme.board.tile.highlightColour);
            } else {
                if (game.hintModeColor()) {
                    int weight = game.getWeight(pos);
                    int colour = weight == 0 ? theme.board.tile.hintModeUnusableLetterBackgroundColour : theme.board.tile.getHintModeGradientColour((float) weight / maxWeight);
                    p.setColor(colour);
                } else {
                    p.setColor(theme.board.tile.backgroundColour);
                }
            }

            float left = boxsize * x;
            float top = boxsize * y;
            float right = boxsize * (x + 1);
            float bottom = boxsize * (y + 1);
            canvas.drawRect(left, top, right, bottom, p);
        }

        // Draw grid, but exclude the first and last line (both horizontally and vertically unless asked)
        p.setColor(theme.board.tile.borderColour);
        p.setStrokeWidth(theme.board.tile.borderWidth);

        // Vertical lines
        for (float i = boxsize; i <= gridsize - boxsize; i += boxsize) {
            canvas.drawLine(i, 0, i, gridsize, p);
        }
        // Horizontal lines
        for (float i = boxsize; i <= gridsize - boxsize; i += boxsize) {
            canvas.drawLine(0, i, gridsize, i, p);
        }

        if (theme.board.hasOuterBorder) {
            p.setStyle(Paint.Style.STROKE);
            p.setColor(theme.board.tile.borderColour);
            p.setStrokeWidth(theme.board.tile.borderWidth);
            canvas.drawRect(0, 0, width, height, p);

            p.setStyle(Paint.Style.FILL);
        }

        p.setColor(theme.board.tile.foregroundColour);
        p.setTypeface(Fonts.get().getSansSerifCondensed());
        float textSize = boxsize * 0.8f;
        p.setTextSize(textSize);

        // Find vertical center offset
        p.getTextBounds("A", 0, 1, textBounds);
        float offset = textBounds.exactCenterY();

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardWidth; y++) {
                int pos = game.getLetterGrid().getRotatedPosition(y * boardWidth + x);
                int weight = game.getWeight(pos);

                if (game.hintModeColor() || game.hintModeCount()) {
                    int colour = (weight == 0) ? theme.board.tile.hintModeUnusableLetterColour : theme.board.tile.foregroundColour;
                    p.setColor(colour);
                } else {
                    p.setColor(theme.board.tile.foregroundColour);
                }

                if (game.hintModeCount()) {
                    p.setTextSize(textSize / 4);
                    p.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("" + weight, (x * boxsize) + 8, ((y + 1) * boxsize) - 6, p);
                }

                String letter = game.getLetterGrid().elementAt(x, y);
                String letterForDisplay = game.getLanguage().toDisplay(letter);
                p.setTextSize(textSize);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(letterForDisplay, (x * boxsize) + (boxsize / 2), (y * boxsize) + (boxsize / 2) - offset, p);
                if (GameMode.SCORE_LETTERS.equals(game.getScoreType())) {
                    String score = String.valueOf(game.getLanguage().getPointsForLetter(letter));
                    p.setTextSize(textSize / 4);
                    p.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(score, ((x + 1) * boxsize) - 8, ((y + 1) * boxsize) - 6, p);
                }
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        setDimensions(getMeasuredWidth(), getMeasuredHeight());

        drawBoard(canvas);
    }

}
