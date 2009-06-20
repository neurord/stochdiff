package org.catacomb.numeric.data;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface NameSet {

String targetType() default "";

}
