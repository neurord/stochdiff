package org.catacomb.druid.xtext.parse;


import org.catacomb.druid.xtext.base.ContainerBlock;
import org.catacomb.report.E;


public class TextSplitter {

    //String sourceText;

    public TextSplitter(String txt) {
        //  sourceText = txt;
    }




    public ContainerBlock makeBlock() {
        ContainerBlock bdoc = new ContainerBlock();
//      bdoc.setGuise(StandardGuises.getDocument());

        E.missing();

        /*

        for (Paragraph par : Paragraphizer.paragraphize(sourceText)) {
              ContainerBlock bp = new ContainerBlock();
        //            bp.setGuise(StandardGuises.getParagraph());

              for (Sentence sen : Sentencizer.sentencize(par.getText())) {
                 ContainerBlock bs = new ContainerBlock();
        //               bs.setGuise(StandardGuises.getSentence());

                 for (Term w : Termizer.termize(sen.getText() + " ")) {
                    if (w.isWord()) {
                       WordBlock bw = new WordBlock();
        //                     bw.setGuise(StandardGuises.getWord());
                       bw.setText(w.getText());
                       bs.addBlock(bw);
                    } else {
                        PunctuationBlock bpunc = new PunctuationBlock();
        //                      bpunc.setGuise(StandardGuises.getWord());
                        bpunc.setText(w.getText());
                        bs.addBlock(bpunc);
                    }
                 }


                 bp.addBlock(bs);
              }
              bdoc.addBlock(bp);
              bdoc.addBlock(new NewlineBlock());
              bdoc.addBlock(new NewlineBlock());
        }

        bdoc.link();
        */

        return bdoc;
    }




}
