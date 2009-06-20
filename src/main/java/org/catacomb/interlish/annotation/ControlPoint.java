package org.catacomb.interlish.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface ControlPoint {

    String xid();


}


