package hr.prism.board.authentication;

import hr.prism.board.domain.Scope;
import hr.prism.board.enums.Action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Restriction {
    
    Scope scope();
    
    Action[] actions() default {};
    
}
