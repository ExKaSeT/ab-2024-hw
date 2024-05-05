package edu.example.springmvcdemo.validation.constraints;

import edu.example.springmvcdemo.validation.validators.ImageExtensionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageExtensionValidator.class)
public @interface ImageExtensionConstraint {

    String message() default "Image extension not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}