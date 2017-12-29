package hr.prism.board.enums;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Labels {

    Label[] value();
    @Repeatable(Labels.class)
    @interface Label {

        Scope scope();

        String value();

    }

}
