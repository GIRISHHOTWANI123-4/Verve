package com.example.verve.controller;

import com.example.verve.service.VerveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/verve")
public class VerveController {

    @Autowired
    private final VerveService verveService;

    @Autowired
    public VerveController(VerveService verveService) {
        this.verveService = verveService;
    }

    @GetMapping("/accept")
    public ResponseEntity<String> acceptRequest(
            @RequestParam("id") int id,
            @RequestParam(required = false) String endpoint) {
        try {
            verveService.processRequest(id, endpoint);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
        }
    }
}
