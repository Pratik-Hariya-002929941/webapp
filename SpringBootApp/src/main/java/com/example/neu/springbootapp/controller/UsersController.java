package com.example.neu.springbootapp.controller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.example.neu.springbootapp.config.StatsdClient;
import com.example.neu.springbootapp.model.OneTimeToken;
import com.example.neu.springbootapp.model.Users;
import com.example.neu.springbootapp.repository.OneTimeTokenRepository;
import com.example.neu.springbootapp.repository.UsersRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@Validated
@RestController
@RequestMapping("/v1/account")
public class UsersController {

    @Autowired
    private final UsersRepository usersRepository;

    @Autowired
            private final OneTimeTokenRepository oneTimeTokenRepository;

    //private static final Logger logger = Logger.getLogger(UsersController.class.getName());
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static StatsdClient statsDClient;

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Logger logger = LoggerFactory.getLogger(UsersController.class);
    public UsersController(UsersRepository usersRepository, OneTimeTokenRepository oneTimeTokenRepository) {
        this.usersRepository = usersRepository;
        this.oneTimeTokenRepository = oneTimeTokenRepository;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Users> getUserAccountInformation(@PathVariable(value = "accountId") UUID id,
                                                           @RequestHeader Map<String, String> headers) {

        logger.info("Reached: GET /v1/account/" + id);
        statsDClient.increment("endpoint.http.getAccount");

        String authorization = null;
        JSONObject jsonObj =new JSONObject();

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            jsonObj.put("error", "Missing Authorization Header ");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));

        if(pair.split(":").length < 2){
            jsonObj.put("error", "Username and Password can not be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String userName =pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Fetching Details for accountID: " + id);

        Users users = usersRepository.findById(id);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!users.getUsername().equals(userName)){
            jsonObj.put("error", "You are not authorized to updated");
            return new ResponseEntity(jsonObj, HttpStatus.FORBIDDEN);
        }

        if(!BCrypt.checkpw(password, users.getPassword()) || !users.getUsername().equals(userName)) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Successfully Fetched Data: " + users);

        return new ResponseEntity(users, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity createUserAccount(@Valid @RequestBody Users account){

        logger.info("Reached: POST /v1/account  " + account);
        statsDClient.increment("endpoint.http.postAccount");

        JSONObject jsonObj = new JSONObject();
        Users users = usersRepository.findByUsername(account.getUsername());

        if(users != null) {
            jsonObj.put("error", "Username must be unique ");
            logger.error("Username already exists");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(account.getId() != null) {
            jsonObj.put("error", "ID is a readonly field");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(account.getAccountCreated() != null) {
            jsonObj.put("error", "Account created is a readonly field");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(account.getAccountUpdated() != null) {
            jsonObj.put("error", "Account Updated is a readonly field");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(account.getPassword() == null || account.getPassword().equals("")) {
            jsonObj.put("error", "Password cannot be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(account.getFirstName() == null || account.getFirstName().equals("")) {
            jsonObj.put("error", "First Name cannot be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(account.getLastName() == null || account.getLastName().equals("")) {
            jsonObj.put("error", "Last Name cannot be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users!= null && !users.getUsername().equals("") && !users.getUsername().contains("@") && !users.getUsername().contains(".")) {
            jsonObj.put("error", "Username must be in the form of email");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users!= null && users.getPassword() != null && users.getPassword().isEmpty()) {
            jsonObj.put("error", "Password cannot be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String password = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(10));
        account.setPassword(password);
        Users savedAccount = usersRepository.save(account);
        logger.info("Successfully Saved Data: " + savedAccount);

        try {
            long now = Instant.now().getEpochSecond(); // unix time
            long ttl = 60*2; // 2 minutes in sec

            OneTimeToken oneTimeToken = new OneTimeToken();
            oneTimeToken.setEmail(savedAccount.getUsername());
            oneTimeToken.setExpiry(now+ttl);

            logger.info("OneTimeToken before save: " + oneTimeToken);
            oneTimeToken = oneTimeTokenRepository.createOneTimeToken(oneTimeToken);

            logger.info("Successfully Saved OneTimeToken: " + oneTimeToken);
        }
        catch (AmazonServiceException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage(), e);
        }
        catch (AmazonClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        return new ResponseEntity(savedAccount, HttpStatus.OK);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Users> updateAccount(@PathVariable(value = "accountId") UUID id, @RequestBody Users users, @RequestHeader Map<String, String> headers){

        logger.info("Reached: PUT /v1/account/" + id);
        statsDClient.increment("endpoint.http.putAccount");

        JSONObject jsonObj = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization")) {
            authorization = headers.get("authorization");
            logger.info("Authorization Method Used: " + authorization.split(" ")[0]);
        }else{
            jsonObj.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));

        if(pair.split(":").length < 2){
            jsonObj.put("error", "Username and Password can not be empty");
            logger.info("Username and Password can not be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String username=pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Updating Details for accountID: " + id);

        Users accountDetails = usersRepository.findById(id);

        if(authorization == null){
            jsonObj.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(accountDetails == null) {
            jsonObj.put("error", "Invalid User ID");
            logger.info("Invalid User ID");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!accountDetails.getUsername().equals(username)){
            jsonObj.put("error", "You are not authorized to updated");
            logger.info("You are not authorized to updated");
            return new ResponseEntity(jsonObj, HttpStatus.FORBIDDEN);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())  || !accountDetails.getUsername().equals(username)) {
            jsonObj.put("error", "User is not Authorized");
            logger.info("User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if( users.getUsername() != null) {
            jsonObj.put("error", "Username cannot be updated");
            logger.info("Username cannot be updated");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getId() != null) {
            jsonObj.put("error", "Id can not be updated");
            logger.info("Id can not be updated");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getAccountCreated() != null) {
            jsonObj.put("error", "Can not set account create time");
            logger.info("Can not set account create time");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getAccountUpdated() != null) {
            jsonObj.put("error", "Can not set account update time");
            logger.info("Can not set account update time");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users!= null && users.getUsername() != null && !users.getUsername().equals("") && !users.getUsername().contains("@") && !users.getUsername().contains(".")) {
            jsonObj.put("error", "Username must be in the form of email");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getPassword() == null || users.getPassword().equals("")) {
            jsonObj.put("error", "Password cannot be empty");
            logger.info("Password can not be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if( users.getFirstName() != null && !users.getFirstName().isEmpty())
            accountDetails.setFirstName(users.getFirstName());
        if( users.getLastName() != null && !users.getLastName().isEmpty())
            accountDetails.setLastName(users.getLastName());
        if( users.getPassword() != null && !users.getPassword().isEmpty())
            accountDetails.setPassword(BCrypt.hashpw(users.getPassword(), BCrypt.gensalt(10)));
        Users updatedAccountDetails = usersRepository.save(accountDetails);

        logger.info("Successfully Updated Data: " + updatedAccountDetails);

        return new ResponseEntity(updatedAccountDetails, HttpStatus.OK);
    }
}