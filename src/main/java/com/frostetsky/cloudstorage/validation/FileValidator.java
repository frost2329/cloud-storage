package com.frostetsky.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import static com.frostetsky.cloudstorage.util.ValidationUtil.buildViolation;


public class FileValidator implements ConstraintValidator<File, MultipartFile[]> {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public boolean isValid(MultipartFile[] files, ConstraintValidatorContext context) {
        if (files == null || files.length == 0) {
            buildViolation(context, "Необходимо загрузить хотя бы один файл");
            return false;
        }
        for (MultipartFile file : files) {
            if (file == null) {
                buildViolation(context, "Файл не должен быть пустым");
                return false;
            }
            if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
                buildViolation(context, "Имя файла не может быть пустым");
                return false;
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                buildViolation(context,
                        String.format("Файл '%s' превышает максимальный размер 10MB", file.getOriginalFilename()));
                return false;
            }
        }
        return true;
    }


}
