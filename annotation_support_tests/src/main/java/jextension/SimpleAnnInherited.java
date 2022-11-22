package jextension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Inherited
@Retention(RUNTIME)
@ExtendWith(SimpleExt.class)
public @interface SimpleAnnInherited { }
