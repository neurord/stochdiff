package org.catacomb.druid.xtext.canvas;


import org.catacomb.druid.xtext.base.*;
import org.catacomb.report.E;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class KeyWriter implements KeyListener {

    TextBoard textBoard;

    int keyCode;

    TextBlock caretBlock;
    int caretPos;


    public KeyWriter(TextBoard tb) {
        textBoard = tb;
    }


    public TextBlock getCaretBlock() {
        return caretBlock;
    }


    public int getCaretPos() {
        return caretPos;
    }


    public void setCaretBlock(TextBlock tb) {
        caretBlock = tb;
    }


    public void setCaretPos(int ip) {
        caretPos = ip;
    }


    public void keyPressed(KeyEvent e) {
        keyCode = e.getKeyCode();
        switch (keyCode) {
        case KeyEvent.VK_LEFT:
            caretLeft();
            break;
        case KeyEvent.VK_RIGHT:
            caretRight();
            break;
        case KeyEvent.VK_UP:
            caretUp();
            break;
        case KeyEvent.VK_DOWN:
            caretDown();
            break;
        case KeyEvent.VK_BACK_SPACE:
            deletBackwards();
            break;

        case KeyEvent.VK_ENTER:
            newline();
            break;

        default:

        }
    }



    public void keyTyped(KeyEvent e) {

        if (e.isActionKey()) {
            E.info("action key " + KeyEvent.getKeyText(keyCode));

        } else {
            char c = e.getKeyChar();
            insertCharacter(c);
        }
    }


    public void keyReleased(KeyEvent e) {

    }



    public void caretLeft() {
        if (caretBlock == null) {
            return;
        }
        if (caretPos > 0) {
            caretPos -= 1;
        } else {
            TextBlock tb = caretBlock.previousTextBlock();
            if (tb == null) {

            } else {
                caretBlock = tb;
                caretPos = caretBlock.textLength() - 1;
            }
        }

        textBoard.repaint();
    }


    public void caretRight() {
        if (caretBlock == null) {
            return;
        }
        if (caretPos < caretBlock.textLength()) {
            caretPos += 1;
        } else {
            TextBlock tb = caretBlock.nextTextBlock();
            if (tb == null) {

            } else {
                caretBlock = tb;
                caretPos = 1;
            }
        }
        textBoard.repaint();
    }


    public void caretUp() {
        if (caretBlock == null) {
            return;
        }

    }


    public void caretDown() {
        if (caretBlock == null) {
            return;
        }

    }


    public void insertCharacter(char c) {
        String s = "" + c;
        if (Character.isLetter(c)) {
            insertLetter(c);
            textBoard.repaint();

        } else if (TextBoard.PUNCTUATION.indexOf(s) >= 0) {
            insertPunctuation(c);
            textBoard.repaint();

        } else {
            // E.info("ignoring funny character " + s);
        }
    }



    private void insertLetter(char c) {
        if (caretBlock instanceof WordBlock) {
            insertLetterInWord(caretBlock, c, caretPos);

        } else {
            if (caretPos == 0) {
                insertLetterBeforeNonWord(caretBlock, c);

            } else if (caretPos == caretBlock.textLength()) {
                insertLetterAfterNonWord(caretBlock, c);

            } else {
                insertLetterInNonWord(caretBlock, c, caretPos);
            }
        }
    }


    private void insertLetterInWord(TextBlock tb, char c, int cp) {
        tb.insertCharacter(c, cp);
        caretBlock = tb;
        caretPos = cp + 1;
    }


    private void insertLetterInNonWord(TextBlock tb, char c, int cp) {
        WordBlock wb = new WordBlock();
        wb.setText("" + c);
        tb.insert(wb, cp);
        caretBlock = wb;
        caretPos = 1;
    }


    private void insertLetterBeforeNonWord(TextBlock tb, char c) {
        TextBlock bpr = tb.previousTextBlock();
        if (bpr instanceof WordBlock) {
            insertLetterInWord(bpr, c, bpr.textLength());

        } else {
            addNewWordBlockAfter(bpr, c);
        }
    }


    private void addNewWordBlockAfter(TextBlock bpr, char c) {
        WordBlock wb = new WordBlock();
        wb.setText("" + c);
        Block bnx = bpr.next();
        bpr.setNext(wb);
        wb.setNext(bnx);
        caretBlock = wb;
        caretPos = 1;
    }



    private void insertLetterAfterNonWord(TextBlock tb, char c) {
        TextBlock bnx = tb.nextTextBlock();
        if (bnx instanceof WordBlock) {
            insertLetterInWord(bnx, c, 0);

        } else {
            addNewWordBlockAfter(tb, c);
        }
    }



    private void insertPuncInPunc(TextBlock tb, char c, int cp) {
        tb.insertCharacter(c, cp);
        caretBlock = tb;
        caretPos = cp + 1;
    }



    private void insertPunctuation(char c) {
        if (caretBlock instanceof PunctuationBlock) {
            insertPuncInPunc(caretBlock, c, caretPos);

        } else {
            if (caretPos == 0) {
                insertPuncBeforeNonPunc(caretBlock, c);

            } else if (caretPos == caretBlock.textLength()) {
                insertPuncAfterNonPunc(caretBlock, c);
            } else {
                insertPuncInNonPunc(caretBlock, c, caretPos);
            }
        }
    }



    private void insertPuncInNonPunc(TextBlock tb, char c, int cp) {
        PunctuationBlock wb = new PunctuationBlock();
        wb.setText("" + c);
        tb.insert(wb, cp);
        caretBlock = wb;
        caretPos = 1;
    }


    private void insertPuncBeforeNonPunc(TextBlock tb, char c) {
        TextBlock bpr = tb.previousTextBlock();
        if (bpr instanceof PunctuationBlock) {
            insertPuncInPunc(bpr, c, bpr.textLength());

        } else {
            addNewPuncBlockAfter(bpr, c);
        }
    }


    private void addNewPuncBlockAfter(TextBlock bpr, char c) {
        PunctuationBlock wb = new PunctuationBlock();
        wb.setText("" + c);
        Block bnx = bpr.next();
        bpr.setNext(wb);
        wb.setNext(bnx);
        caretBlock = wb;
        caretPos = 1;
    }



    private void insertPuncAfterNonPunc(TextBlock tb, char c) {
        TextBlock bnx = tb.nextTextBlock();
        if (bnx instanceof PunctuationBlock) {
            insertPuncInPunc(bnx, c, 0);

        } else {
            addNewPuncBlockAfter(tb, c);
        }
    }



    public void deletBackwards() {
        if (caretPos == 0) {
            TextBlock tb = caretBlock.previousTextBlock();
            if (tb == null) {
                return;

            } else {
                caretBlock = tb;
                caretPos = caretBlock.textLength();
            }
        }

        if (caretBlock.textLength() == 1) {
            TextBlock tb = caretBlock.previousTextBlock();
            int newPos = tb.textLength();
            caretBlock.remove();
            if (tb == null) {
                caretBlock = textBoard.firstTextBlock();
                caretPos = 0;
            } else {
                caretBlock = tb;
                caretPos = newPos;
            }

        } else {
            caretBlock.deleteCharBefore(caretPos);
            caretPos -= 1;
        }

        textBoard.repaint();
    }



    private void newline() {
        if (caretPos == 0) {
            TextBlock tbp = caretBlock.previousTextBlock();
            if (tbp != null) {
                tbp.newlineAfter();
            }

        } else if (caretPos == caretBlock.textLength()) {
            caretBlock.newlineAfter();
            caretBlock = caretBlock.nextTextBlock();
            caretPos = 1;

        } else {
            caretBlock.insertNewline(caretPos);
            caretBlock = caretBlock.nextTextBlock();
            caretPos = 1;
        }
    }



    public boolean hasCaret() {
        return (caretBlock != null);
    }



}
