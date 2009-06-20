package org.catacomb.numeric.data;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface QuantityDA {

String unit() default "int";

    String title();

    String nameSet();

}
