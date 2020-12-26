package cn.ots.alarm.utils.annotation;

import javax.xml.ws.BindingType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBase {

    String name() default "";

    String[] valid() default {};

    int minLength() default 0;

    int maxLength() default Integer.MAX_VALUE;
}
