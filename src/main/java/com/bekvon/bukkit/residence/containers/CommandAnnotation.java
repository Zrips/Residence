package com.bekvon.bukkit.residence.containers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface CommandAnnotation {
    boolean simple() default true;

    int priority() default 1000;

    String info() default "";

    String[] usage() default "";

    String[] explanation() default {};

    String[] tab() default {};

    int[] regVar() default {};

    int[] consoleVar() default {};
}
