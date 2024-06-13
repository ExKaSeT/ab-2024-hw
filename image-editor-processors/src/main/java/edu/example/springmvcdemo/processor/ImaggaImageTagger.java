package edu.example.springmvcdemo.processor;

import edu.example.springmvcdemo.config.ImaggaImageTaggerConfig;
import edu.example.springmvcdemo.dto.imagga_image_tagger.ImaggaGetTagsResponseDto;
import edu.example.springmvcdemo.dto.imagga_image_tagger.ImaggaImageUploadResponseDto;
import edu.example.springmvcdemo.dto.imagga_image_tagger.TagDto;
import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.RetryableStatusCodeException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ImaggaImageTagger implements ImageProcessor {
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final int MAX_TAG_COUNT = 3;
    private final RestClient restClient;
    private final String authHeaderValue;
    private final URI uploadImageUri;
    private final String getImageTagsUri;

    public ImaggaImageTagger(RestClient restClient, ImaggaImageTaggerConfig config) {
        this.restClient = restClient;
        this.authHeaderValue = getAuthHeader(config.getApiKey(), config.getApiSecret());
        this.uploadImageUri = URI.create(config.getUploadImageUrl());
        this.getImageTagsUri = URI.create(config.getGetImageTagsUrl()) + "?image_upload_id={imageId}";
    }

    @Override
    public StreamDataDto process(InputStream imageStream, String imageExtension) throws IOException {
        var imageBytes = imageStream.readAllBytes();
        var imaggaImageId = uploadImage(new ByteArrayInputStream(imageBytes));
        var tags = getImageTags(imaggaImageId);
        var result = drawTags(new ByteArrayInputStream(imageBytes), tags);

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(result, imageExtension, outputStream);
        return new StreamDataDto(outputStream);
    }

    private String uploadImage(InputStream inputStream) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("image", new InputStreamResource(inputStream));

        var responseDto = restClient.post()
                .uri(uploadImageUri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(AUTH_HEADER_NAME, authHeaderValue)
                .body(form)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus((code) -> code.is5xxServerError() || code.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS),
                        (request, response) -> {
                            throw new RetryableStatusCodeException("ImaggaImageTagger::uploadImage",
                                    response.getStatusCode().value(), response, uploadImageUri);
                        })
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new HttpClientErrorException(response.getStatusCode(), response.getStatusText());
                })
                .body(ImaggaImageUploadResponseDto.class);

        assert responseDto != null;
        return responseDto.getResult().getUploadId();
    }

    private List<TagDto> getImageTags(String imageId) {
        var responseDto = restClient.get()
                .uri(getImageTagsUri, imageId)
                .header(AUTH_HEADER_NAME, authHeaderValue)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus((code) -> code.is5xxServerError() || code.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS),
                        (request, response) -> {
                            throw new RetryableStatusCodeException("ImaggaImageTagger::getImageTags",
                                    response.getStatusCode().value(), response, uploadImageUri);
                        })
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new HttpClientErrorException(response.getStatusCode(), response.getStatusText());
                })
                .body(ImaggaGetTagsResponseDto.class);

        assert responseDto != null;
        return Arrays.stream(responseDto.getResult().getTags())
                .sorted(Comparator.comparingDouble(ImaggaGetTagsResponseDto.Tag::getConfidence).reversed())
                .limit(MAX_TAG_COUNT)
                .map(tag -> new TagDto(tag.getTag().getEn(), tag.getConfidence()))
                .collect(Collectors.toList());
    }

    private String getAuthHeader(String apiKey, String apiSecret) {
        String toEncode = String.format("%s:%s", apiKey, apiSecret);
        String basicAuth = Base64.getEncoder().encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
        return "Basic " + basicAuth;
    }

    private BufferedImage drawTags(InputStream stream, List<TagDto> tags) throws IOException {
        String text = tags.stream()
                .map(tag -> String.format("%s(%.2f%%)", tag.getName(), tag.getProbability()))
                .collect(Collectors.joining(", ", " ", " "));

        BufferedImage image = ImageIO.read(stream);
        Graphics graphics = image.getGraphics();

        Font font = new Font("Arial", Font.BOLD, 28);
        AttributedString attributedText = new AttributedString(text);
        attributedText.addAttribute(TextAttribute.FONT, font);
        attributedText.addAttribute(TextAttribute.BACKGROUND, Color.BLACK);
        attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);

        FontMetrics metrics = graphics.getFontMetrics(font);
        int posX = 0;
        int posY = image.getHeight() - metrics.getHeight() + metrics.getAscent();
        graphics.drawString(attributedText.getIterator(), posX, posY);

        return image;
    }
}
