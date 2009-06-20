package org.catacomb.interlish.structure;


public interface MovieController extends MovieStateDisplay {

    void setMovieOperator(MovieOperator mop);


    void syncFromOperator();


}
