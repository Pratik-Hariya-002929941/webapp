package com.example.neu.springbootapp.controller;

import com.example.neu.springbootapp.model.Users;
import com.example.neu.springbootapp.repository.UsersRepository;
import org.apache.catalina.User;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Component
@Validated
@RestController
@RequestMapping("/v1/account")
public class UsersController {

    @Autowired
    private final UsersRepository usersRepository;

    private static final Logger logger = Logger.getLogger(UsersController.class.getName());
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsersController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Users> getUserAccountInformation(@PathVariable(value = "accountId") UUID id,
                                                           @RequestHeader Map<String, String> headers) {

        String authorization = null;
        JSONObject jsonObj =new JSONObject();

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            jsonObj.put("error", "Missing Authorization Header ");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        String userName =pair.split(":")[0];
        String password= pair.split(":")[1];
        Users users = usersRepository.findById(id);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, users.getPassword()) || !users.getUsername().equals(userName)) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity(users, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity createUserAccount(@Valid @RequestBody Users account){

        JSONObject jsonObj = new JSONObject();
        Users users = usersRepository.findByUsername(account.getUsername());

        if(users != null) {
            jsonObj.put("error", "Username must be unique ");
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

        String password = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt(10));
        account.setPassword(password);
        Users savedAccount = usersRepository.save(account);
        return new ResponseEntity(savedAccount, HttpStatus.OK);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Users> updateAccount(@PathVariable(value = "accountId") UUID id, @RequestBody Users users, @RequestHeader Map<String, String> headers){

        JSONObject jsonObj = new JSONObject();
        String authorization = null;

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            jsonObj.put("error", "Missing Authorization Header ");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));
        String username=pair.split(":")[0];
        String password= pair.split(":")[1];
        Users accountDetails = usersRepository.findById(id);

        if(authorization == null){
            jsonObj.put("error", "Missing Authorization Header ");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(accountDetails == null) {
            jsonObj.put("error", "Invalid User ID");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, accountDetails.getPassword())  || !accountDetails.getUsername().equals(username)) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if( users.getUsername() != null) {
            jsonObj.put("error", "Username cannot be updated");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getId() != null) {
            jsonObj.put("error", "Id can not be updated");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getAccountCreated() != null) {
            jsonObj.put("error", "Can not set account create time");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.getAccountUpdated() != null) {
            jsonObj.put("error", "Can not set account update time");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if( users.getFirstName() != null && !users.getFirstName().isEmpty())
            accountDetails.setFirstName(users.getFirstName());
        if( users.getLastName() != null && !users.getLastName().isEmpty())
            accountDetails.setLastName(users.getLastName());
        if( users.getPassword() != null && !users.getPassword().isEmpty())
            accountDetails.setPassword(BCrypt.hashpw(users.getPassword(), BCrypt.gensalt(10)));
        Users updatedAccountDetails = usersRepository.save(accountDetails);
        return new ResponseEntity(updatedAccountDetails, HttpStatus.OK);
    }
}