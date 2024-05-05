package edu.example.springmvcdemo.validation.validators;

import edu.example.springmvcdemo.model.ImageProcessingFilter;
import edu.example.springmvcdemo.validation.constraints.ImageFilterConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageFilterValidator implements ConstraintValidator<ImageFilterConstraint, String> {

    @Override
    public boolean isValid(String filter, ConstraintValidatorContext constraintValidatorContext) {

        try {
            ImageProcessingFilter.valueOf(filter);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}