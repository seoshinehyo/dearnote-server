package com.dearnote.web.controller;

import com.dearnote.apipayload.ApiResponse;
import com.dearnote.converter.ImageConverter;
import com.dearnote.domain.Image;
import com.dearnote.domain.Letter;
import com.dearnote.service.aws.S3Service;
import com.dearnote.service.image.ImageCommandService;
import com.dearnote.service.image.ImageQueryService;
import com.dearnote.service.letter.LetterQueryService;
import com.dearnote.validation.annotation.ExistImage;
import com.dearnote.validation.annotation.ExistLetter;
import com.dearnote.web.dto.image.ImageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/dearnote")
@RequiredArgsConstructor
public class ImageRestController {

    private final S3Service s3Service;
    private final ImageCommandService imageCommandService;
    private final LetterQueryService letterQueryService;
    private final ImageQueryService imageQueryService;

    @PostMapping(value = "/images", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "이미지 업로드 API", description = "이미지를 업로드하고, 업로드된 이미지의 S3 URL을 반환하는 api입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON500", description = "서버 에러, 관리자에게 문의 바랍니다.")
    })
    @Parameters({
            @Parameter(name = "letterId", description = "업로드 할 편지의 아이디, path variable 입니다.")
    })
    public ApiResponse<ImageResponseDTO.RegistImageResponseDTO> uploadImage(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("image") MultipartFile file,
            @ExistLetter @RequestParam(value = "letterId") Long letterId) {
        try {
            String storeFileUrl = uploadToS3(file);
            Letter letter = fetchLetter(letterId);
            Image image = saveImage(file, storeFileUrl, letter);

            return ApiResponse.onSuccess(ImageConverter.toRegistImageDTO(image, letter));
        } catch (IOException e) {
            return ApiResponse.onFailure("IMAGE_UPLOAD_FAILED", "이미지 업로드 중 오류가 발생했습니다.", null);
        }
    }

    @GetMapping("/{letterId}/images")
    @Operation(summary = "이미지 조회 API", description = "편지에 등록된 이미지를 조회하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "letterId", description = "이미지가 등록된 편지의 아이디, path variable 입니다.")
    })
    public ApiResponse<ImageResponseDTO.GetImageResponseDTO> getImage(@ExistLetter @PathVariable Long letterId) {
        Letter letter = letterQueryService.getLetter(letterId);
        Image image = imageQueryService.getImageByLetter(letter);

        return ApiResponse.onSuccess(ImageConverter.toGetImageDTO(image, letter));
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "이미지 삭제 API", description = "이미지를 삭제하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200",description = "OK, 성공"),
    })
    @Parameters({
            @Parameter(name = "imageId", description = "삭제할 사진의 아이디, path variable 입니다.")
    })
    public ApiResponse<Void> delete(@ExistImage @PathVariable Long imageId) {
        Image image = imageQueryService.getImage(imageId);
        imageCommandService.deleteImage(image);
        return ApiResponse.onSuccess(null);
    }

    private String uploadToS3(MultipartFile file) throws IOException {
        return s3Service.uploadFile(file);
    }

    private Letter fetchLetter(Long letterId) {
        return letterQueryService.getLetter(letterId);
    }

    private Image saveImage(MultipartFile file, String storeFileUrl, Letter letter) {
        String originFileName = file.getOriginalFilename();
        Integer size = Math.toIntExact(file.getSize());
        String storeFileName = extractStoreFileName(storeFileUrl);

        return imageCommandService.saveImage(originFileName, size, storeFileName, storeFileUrl, letter);
    }

    private String extractStoreFileName(String storeFileUrl) {
        return storeFileUrl.substring(storeFileUrl.lastIndexOf("/") + 1);
    }
}
