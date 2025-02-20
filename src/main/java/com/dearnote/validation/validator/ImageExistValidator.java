package com.dearnote.validation.validator;

import com.dearnote.apipayload.code.status.ErrorStatus;
import com.dearnote.repository.ImageRepository;
import com.dearnote.validation.annotation.ExistImage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageExistValidator implements ConstraintValidator<ExistImage, Long> {

    private final ImageRepository imageRepository;

    @Override
    public void initialize(ExistImage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {

        // 이미지 id가 null이거나, 레포지토리에 존재하지 않으면 에러
        if (value == null || !imageRepository.existsById(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.IMAGE_NOT_FOUND.toString())
                    .addConstraintViolation();
            return false;
        }

        return true; // 검증 성공
    }
}