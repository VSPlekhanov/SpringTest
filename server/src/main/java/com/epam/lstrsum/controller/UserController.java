package com.epam.lstrsum.controller;

import com.epam.lstrsum.annotation.NotEmptyString;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.TelescopeService;
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
    final private TelescopeService telescopeService;

    @RequestMapping
    public List<User> getListOfUsers() {
        return userService.findAll();
    }

    @GetMapping("/telescope/info")
    public ResponseEntity<List<TelescopeEmployeeEntityDto>> getUserInfoByFullName(
            @NotEmptyString @RequestParam String fullName,
            @Max(5000) @Min(0) @RequestParam(required = false, defaultValue = "5") Integer maxUsersAmountInResult
    ) {
        return ResponseEntity.ok(telescopeService.getUsersInfoByFullName(fullName, maxUsersAmountInResult));
    }

    @GetMapping("/telescope/photo")
    public ResponseEntity<String> getUserPhotoByUri(@NotEmptyString @RequestParam String uri) {
        return ResponseEntity.ok(telescopeService.getUserPhotoByUri(uri));
    }
}
