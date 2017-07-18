package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.RequestAllFieldsDto;
import com.epam.lstrsum.dto.RequestPostDto;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.security.EpamEmployeePrincipal;
import com.epam.lstrsum.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/request")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping()
    public ResponseEntity<RequestAllFieldsDto> addRequest(Authentication authentication, @RequestBody() RequestPostDto dtoObject)
            throws IOException {
        String email = authentication != null ? ((EpamEmployeePrincipal) (authentication.getPrincipal())).getEmail() : "John_Doe@epam.com";
        Request request = requestService.addNewRequest(dtoObject, email);
        RequestAllFieldsDto requestAllFieldsDto = requestService.requestToDto(request);
        return ResponseEntity.ok(requestAllFieldsDto);
    }

    @GetMapping(value = "/{requestId}")
    public ResponseEntity<RequestAllFieldsDto> getRequest(Authentication authentication, @PathVariable String requestId) {
        if (requestService.contains(requestId)) {
            RequestAllFieldsDto requestDto = requestService.getRequestDtoByRequestId(requestId);
            return ResponseEntity.ok(requestDto);
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/list")
    public ResponseEntity<List<RequestAllFieldsDto>> getRequests(Authentication authentication) {
        List<RequestAllFieldsDto> requestDtoList = requestService.findAll();
        return ResponseEntity.ok(requestDtoList);
    }
}
