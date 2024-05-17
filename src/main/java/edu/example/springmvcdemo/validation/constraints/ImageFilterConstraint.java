package edu.example.springmvcdemo.validation.constraints;

import edu.example.springmvcdemo.validation.validators.ImageFilterValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFilterValidator.class)
public @interface ImageFilterConstraint {
    String message() default "The specified filter does not exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}