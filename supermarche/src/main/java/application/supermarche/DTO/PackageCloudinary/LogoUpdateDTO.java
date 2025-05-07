package application.supermarche.DTO.PackageCloudinary;

import org.springframework.web.multipart.MultipartFile;

public record LogoUpdateDTO(
        MultipartFile file
) {
}
