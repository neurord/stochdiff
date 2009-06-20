package org.catacomb.numeric.data;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface Struct {

    String label();

}
