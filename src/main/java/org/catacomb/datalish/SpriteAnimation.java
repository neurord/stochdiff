package org.catacomb.datalish;

public interface SpriteAnimation {


    SceneConfig getSceneConfig(int frameToPaint);

    int getNPoint();

    Box getBox();

    String getFrameDescription(int ifr);

    SpriteStore getSpriteStore();



}
