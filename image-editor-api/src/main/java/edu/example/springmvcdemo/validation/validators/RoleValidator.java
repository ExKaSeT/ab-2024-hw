package edu.example.springmvcdemo.validation.validators;

import edu.example.springmvcdemo.model.Role;
import edu.example.springmvcdemo.validation.constraints.RoleConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import static java.util.Objects.isNull;

public class RoleValidator implements ConstraintValidator<RoleConstraint, String> {

    @Override
    public boolean isValid(String role, ConstraintValidatorContext constraintValidatorContext) {

        if (isNull(role)) {
            return true;
        }

        try {
            Role.valueOf(role);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}