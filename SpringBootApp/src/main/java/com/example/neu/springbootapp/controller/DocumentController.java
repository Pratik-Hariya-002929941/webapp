package com.example.neu.springbootapp.controller;

import com.example.neu.springbootapp.model.Documents;
import com.example.neu.springbootapp.model.Users;
import com.example.neu.springbootapp.repository.DocumentsRepository;
import com.example.neu.springbootapp.repository.StorageRepository;
import com.example.neu.springbootapp.repository.UsersRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Component
@Validated
@RestController
@RequestMapping("/v1/documents")
public class DocumentController {

    @Autowired
    private final StorageRepository storageRepository;
    @Autowired
    private final DocumentsRepository documentsRepository;
    @Autowired
    private final UsersRepository usersRepository;

    private String bucketName = System.getenv("AWS_SBUCKET");

    private static final Logger logger = Logger.getLogger(UsersController.class.getName());
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DocumentController(StorageRepository storageRepository, DocumentsRepository documentsRepository, UsersRepository usersRepository) {
        this.storageRepository = storageRepository;
        this.documentsRepository = documentsRepository;
        this.usersRepository = usersRepository;
    }

    @PostMapping("")
    public ResponseEntity uploadDocument(@RequestParam(value="file") MultipartFile file,  @RequestHeader Map<String, String>headers) {

        String authorization = null;
        JSONObject jsonObj =new JSONObject();
        Documents documents = new Documents();

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

        Users users = usersRepository.findByUsername(userName);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(file.isEmpty()){
            jsonObj.put("error", "Please attach file");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String filename = storageRepository.uploadFile(file, users.getFirstName(), users.getLastName());


        documents.setUserId(users.getId());
        documents.setName(filename);
        documents.setS3_bucket_path(bucketName);
        Documents savedDocument = documentsRepository.save(documents);

        return new ResponseEntity(savedDocument, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity getAllDocuments(@RequestHeader Map<String, String>headers) {

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

        Users users = usersRepository.findByUsername(userName);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        List<Documents> documents = documentsRepository.findByUserId(users.getId());

        return new ResponseEntity(documents, HttpStatus.OK);
    }

    @GetMapping("/{doc_id}")
    public ResponseEntity getDocument(@PathVariable(value = "doc_id") UUID id,
                                      @RequestHeader Map<String, String> headers) {

        Documents documents;
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

        Users users = usersRepository.findByUsername(userName);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        documents = documentsRepository.findById(id);

        if(documents == null) {
            jsonObj.put("error", "Document ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(documents.getUserId() != users.getId()) {
            jsonObj.put("error", "Cannot access document of different user");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(documents, HttpStatus.OK);
    }

    @DeleteMapping("/{doc_id}")
    public ResponseEntity deleteDocument(@PathVariable(value = "doc_id") UUID id,
                                      @RequestHeader Map<String, String> headers) {

        Documents documents;
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

        Users users = usersRepository.findByUsername(userName);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "User is not Authorized");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        documents = documentsRepository.findById(id);

        if(documents == null) {
            jsonObj.put("error", "Document ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.NOT_FOUND);
        }

        if(documents.getUserId() != users.getId()) {
            jsonObj.put("error", "Cannot access document of different user");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String res = storageRepository.deleteFile(documents.getName());
        jsonObj.put("success" ,res);
        documentsRepository.delete(documents);

        return new ResponseEntity(jsonObj, HttpStatus.NO_CONTENT);
    }
}
