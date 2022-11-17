package com.example.neu.springbootapp.controller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.example.neu.springbootapp.model.OneTimeToken;
import com.example.neu.springbootapp.model.Users;
import com.example.neu.springbootapp.repository.OneTimeTokenRepository;
import com.example.neu.springbootapp.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Component
@Validated
@RestController
@RequestMapping("/v1/verifyUserEmail")
public class VerifyEmailController {

    @Autowired
    private final UsersRepository usersRepository;

    @Autowired
    private final OneTimeTokenRepository oneTimeTokenRepository;

    public VerifyEmailController(UsersRepository usersRepository, OneTimeTokenRepository oneTimeTokenRepository) {
        this.usersRepository = usersRepository;
        this.oneTimeTokenRepository = oneTimeTokenRepository;
    }

    @GetMapping("")
    public ResponseEntity<Users> verifyEmail(@RequestParam(value = "email") String email, @RequestParam(value = "token") String token) {

        Users users;
        OneTimeToken oneTimeToken;
        JSONObject jsonObj = new JSONObject();

        Logger logger = LoggerFactory.getLogger(VerifyEmailController.class);
        try {
            logger.info("Entering try block");
            oneTimeToken = oneTimeTokenRepository.getOneTimeToken(email, token);
            logger.info("One time toekn = "+ oneTimeToken);
            if(oneTimeToken == null) {
                jsonObj.put("error", "Verification error");
                return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
            }

            long now = Instant.now().getEpochSecond(); // unix time
            logger.info("db expiry = "+oneTimeToken.getExpiry());
            logger.info("now = "+now);
            if(oneTimeToken.getExpiry() < now) {
                jsonObj.put("error", "Token Expired");
                return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
            }

            users = usersRepository.findByUsername(email);
            logger.info("Users = "+ users);
            if(users == null) {
                jsonObj.put("error", "User not found");
                return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
            }

            users.setVerifiedUser(true);
            usersRepository.save(users);
            logger.info("User saved");
        }
        catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e);
        }
        catch (AmazonClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        return new ResponseEntity(users, HttpStatus.OK);
    }
}
