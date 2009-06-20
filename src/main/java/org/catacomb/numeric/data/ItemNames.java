package org.catacomb.numeric.data;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface ItemNames {

String src() default "none";

}
