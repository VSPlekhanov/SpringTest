package com.epam.lstrsum.controller;

import com.epam.lstrsum.annotation.NotEmptyString;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    final private UserService userService;

    @RequestMapping
    public List<User> getListOfUsers() {
        return userService.findAll();
    }

    /**
     * Performs a request to telescope api to get users info.
     *
     * @param fullName               String name/part name for users elastic search into telescope
     * @param maxUsersAmountInResult Integer value with max users amount in response
     * @return json with users info
     */
    @GetMapping("/telescope/info")
    public ResponseEntity<TelescopeEmployeeEntityDto[]> getUserInfoByFullName(
            @NotEmptyString @RequestParam String fullName,
            @Max(5000) @Min(0) @RequestParam(required = false, defaultValue = "5") Integer maxUsersAmountInResult
    ) {
        return ResponseEntity.ok(userService.getUserInfoByFullName(fullName, maxUsersAmountInResult));
    }

    /**
     * Create a link to telescope to request user photo.
     *
     * @param uri format example "attachment:///upsa_profilePhoto.4060741400007345041_1.GIF_cba0891d-a69f-47c9-96ib-c61a14e6e33d"
     * @return string with link to telescope api to get user photo
     */
    @GetMapping("/telescope/photo")
    public ResponseEntity<String> getUserPhotoByUri(@NotEmptyString @RequestParam String uri) {
        return ResponseEntity.ok(userService.getUserPhotoByUri(uri));
    }
}
