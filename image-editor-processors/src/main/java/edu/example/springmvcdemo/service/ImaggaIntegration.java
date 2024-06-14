package edu.example.springmvcdemo.service;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import edu.example.springmvcdemo.config.ImaggaImageTaggerConfig;
import edu.example.springmvcdemo.dto.imagga_image_tagger.ImaggaGetTagsResponseDto;
import edu.example.springmvcdemo.dto.imagga_image_tagger.ImaggaImageUploadResponseDto;
import edu.example.springmvcdemo.dto.imagga_image_tagger.TagDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.RetryableStatusCodeException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "processor.type", havingValue = "TAGGING")
public class ImaggaIntegration {
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final int MAX_TAG_COUNT = 3;
    private final RestClient restClient;
    private final String authHeaderValue;
    private final URI uploadImageUri;
    private final String getImageTagsUri;

    public ImaggaIntegration(RestClient restClient, ImaggaImageTaggerConfig config) {
        this.restClient = restClient;
        this.authHeaderValue = getAuthHeader(config.getApiKey(), config.getApiSecret());
        this.uploadImageUri = URI.create(config.getUploadImageUrl());
        this.getImageTagsUri = URI.create(config.getGetImageTagsUrl()) + "?image_upload_id={imageId}";
    }

    @Retryable(retryFor = {RetryableStatusCodeException.class}, backoff = @Backoff(delay = 500))
    public String uploadImage(InputStream inputStream) {
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

    @RateLimiting(
            name = "imagga-get-tags-limit",
            ratePerMethod = true
    )
    @Retryable(retryFor = {RetryableStatusCodeException.class}, backoff = @Backoff(delay = 500))
    public List<TagDto> getImageTags(String imageId) {
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
}
