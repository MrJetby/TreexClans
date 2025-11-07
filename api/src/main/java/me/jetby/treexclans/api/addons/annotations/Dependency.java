package me.jetby.treexclans.api.addons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {

    /** Идентификатор зависимого аддона. */
    String id();
}
