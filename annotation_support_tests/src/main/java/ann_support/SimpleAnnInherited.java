package ann_support;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE, ANNOTATION_TYPE })
@Inherited
@Retention(RUNTIME)
@ExtendWith(SimpleExt.class)
public @interface SimpleAnnInherited { }
