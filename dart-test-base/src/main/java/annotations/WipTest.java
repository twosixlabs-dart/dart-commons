package annotations;
import java.lang.annotation.*;
import org.scalatest.TagAnnotation;

// This tag should be used for tests you are currently trying to pass: add the annotation
// @WipTest to a test class or test method to isolate it. You can then run
// sbt "testOnly * -- -n com.twosixlabs.dart.test.tags.annotations.WipTest" to run
// only those tests.

@TagAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface WipTest {}