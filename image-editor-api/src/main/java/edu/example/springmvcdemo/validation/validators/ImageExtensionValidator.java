package edu.example.springmvcdemo.validation.validators;

import edu.example.springmvcdemo.config.AllowedImageExtension;
import edu.example.springmvcdemo.validation.constraints.ImageExtensionConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.web.multipart.MultipartFile;

public class ImageExtensionValidator implements ConstraintValidator<ImageExtensionConstraint, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        String extension = FileNameUtils.getExtension(file.getOriginalFilename());

        if (!AllowedImageExtension.isAllowedIgnoreCase(extension)) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Image extension not allowed")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}