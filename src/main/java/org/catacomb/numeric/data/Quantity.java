package org.catacomb.numeric.data;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Quantity {

String unit() default "int";

    String title();
}
