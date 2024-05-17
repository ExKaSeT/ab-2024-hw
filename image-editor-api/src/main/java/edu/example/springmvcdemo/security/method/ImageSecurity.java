package edu.example.springmvcdemo.security.method;

import edu.example.springmvcdemo.security.UserDetailsImpl;
import edu.example.springmvcdemo.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Objects;

@RequiredArgsConstructor
@Component("imageSecurity")
public class ImageSecurity {

    private final ImageService imageService;

    public boolean isAllowedToModifyImage(Authentication auth, String imageLink) {
        var image = imageService.getMeta(imageLink);
        var user = ((UserDetailsImpl) auth.getPrincipal()).getUser();
        return Objects.equals(image.getUser(), user);
    }
}
