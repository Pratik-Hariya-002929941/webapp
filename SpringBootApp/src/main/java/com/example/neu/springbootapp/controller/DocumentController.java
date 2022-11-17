package com.example.neu.springbootapp.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.example.neu.springbootapp.config.StatsdClient;
import com.example.neu.springbootapp.model.Documents;
import com.example.neu.springbootapp.model.Users;
import com.example.neu.springbootapp.repository.DocumentsRepository;
import com.example.neu.springbootapp.repository.StorageRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Autowired
    private AmazonS3 s3client;

    Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private static StatsdClient statsDClient;

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String bucketName = System.getenv("AWS_SBUCKET");

    //private static final Logger logger = Logger.getLogger(UsersController.class.getName());
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DocumentController(StorageRepository storageRepository, DocumentsRepository documentsRepository, UsersRepository usersRepository) {
        this.storageRepository = storageRepository;
        this.documentsRepository = documentsRepository;
        this.usersRepository = usersRepository;
    }

    @PostMapping("")
    public ResponseEntity uploadDocument(@RequestParam(value="file") MultipartFile file,  @RequestHeader Map<String, String>headers) {

        logger.info("Reached: POST /v1/documents ");
        statsDClient.increment("endpoint.http.postDocument");

        String authorization = null;
        JSONObject jsonObj =new JSONObject();
        Documents documents = new Documents();

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            jsonObj.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header ");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));

        if(pair.split(":").length < 2){
            jsonObj.put("error", "Username and Password can not be empty");
            logger.info("Username and Password can not be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String userName =pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Fetching Account Details for username: " + userName);
        Users users = usersRepository.findByUsername(userName);
        logger.info("Users: " + users);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            logger.info("User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.isVerifiedUser() == false) {
            jsonObj.put("error", "Not a verified user");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "Incorrect Password");
            logger.info("Incorrect Password");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(file.isEmpty()){
            jsonObj.put("error", "Please attach file");
            logger.info("Please attach file");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        logger.info("Uploading file to s3 bucket.......");
        String filename = storageRepository.uploadFile(file, users.getFirstName(), users.getLastName());
        logger.info("File " + filename + " uploaded successfully on S3 Bucket");

        documents.setUserId(users.getId());
        documents.setName(filename);
        documents.setS3_bucket_path(s3client.getUrl(bucketName, filename).toString());

        logger.info("Saving DocumentsDetails to database: " + documents);
        Documents savedDocument = documentsRepository.save(documents);
        logger.info( "Successfully save document data to database: " + documents);

        return new ResponseEntity(savedDocument, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity getAllDocuments(@RequestHeader Map<String, String>headers) {

        logger.info("Reached: GET /v1/documents ");
        statsDClient.increment("endpoint.http.getAllDocument");

        String authorization = null;
        JSONObject jsonObj =new JSONObject();

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
            jsonObj.put("error", "Missing Authorization Header ");
            logger.info("Missing Authorization Header ");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        String pair=new String(Base64.decodeBase64(authorization.substring(6)));

        if(pair.split(":").length < 2){
            jsonObj.put("error", "Username and Password can not be empty");
            logger.info("Username and Password can not be empty");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        String userName =pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Fetching AccountDetails for user: " + userName);
        Users users = usersRepository.findByUsername(userName);
        logger.info("Users: "+ users);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            logger.info("User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.isVerifiedUser() == false) {
            jsonObj.put("error", "Not a verified user");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "Password is incorrect");
            logger.info("Password is incorrect");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Fetching all Documents for user: " + userName);
        List<Documents> documents = documentsRepository.findByUserId(users.getId());

        logger.info("Successfully Retrieve all Documents");
        for(Documents doc: documents)
            logger.info(doc.toString());

        return new ResponseEntity(documents, HttpStatus.OK);
    }

    @GetMapping("/{doc_id}")
    public ResponseEntity getDocument(@PathVariable(value = "doc_id") UUID id,
                                      @RequestHeader Map<String, String> headers) {

        logger.info("Reached: GET /v1/documents/" + id);
        statsDClient.increment("endpoint.http.getDocument");

        Documents documents;
        String authorization = null;
        JSONObject jsonObj =new JSONObject();

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
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

        String userName =pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Fetching AccountDetails for user: " + userName);
        Users users = usersRepository.findByUsername(userName);
        logger.info("Successfully fetched accountDetails: " + users);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            logger.info("User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.isVerifiedUser() == false) {
            jsonObj.put("error", "Not a verified user");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "User is not Authorized");
            logger.info("Password is incorrect");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        documents = documentsRepository.findById(id);

        if(documents == null) {
            jsonObj.put("error", "Document ID not valid");
            logger.info("Document ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(!documents.getUserId().equals(users.getId())) {
            jsonObj.put("error", "Cannot access document of different user");
            logger.info("Cannot access document of different user");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        logger.info("Successfully Retrieve Document Details: " + documents);
        return new ResponseEntity(documents, HttpStatus.OK);
    }

    @DeleteMapping("/{doc_id}")
    public ResponseEntity deleteDocument(@PathVariable(value = "doc_id") UUID id,
                                      @RequestHeader Map<String, String> headers) {

        logger.info("Reached: DELETE /v1/documents/" + id);
        statsDClient.increment("endpoint.http.deleteDocument");

        Documents documents;
        String authorization = null;
        JSONObject jsonObj =new JSONObject();

        if(headers.containsKey("authorization"))
            authorization = headers.get("authorization");
        else{
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

        String userName =pair.split(":")[0];
        String password= pair.split(":")[1];

        logger.info("Fetching accountDetails for user: "+ userName);
        Users users = usersRepository.findByUsername(userName);
        logger.info("Successfully Fetched accountDetails: " + users);

        if(users == null) {
            jsonObj.put("error", "User ID not valid");
            logger.info("User ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        if(users.isVerifiedUser() == false) {
            jsonObj.put("error", "Not a verified user");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        if(!BCrypt.checkpw(password, users.getPassword())) {
            jsonObj.put("error", "Password is incorrect");
            logger.info("Password is incorrect");
            return new ResponseEntity(jsonObj, HttpStatus.UNAUTHORIZED);
        }

        logger.info("Fetching Documents Details by DocumentId...: "+ id);
        documents = documentsRepository.findById(id);
        logger.info("Successfully Fetched DocumentsDetails: " + documents);

        if(documents == null) {
            jsonObj.put("error", "Document ID not valid");
            logger.info("DOcument ID not valid");
            return new ResponseEntity(jsonObj, HttpStatus.NOT_FOUND);
        }

        if(!documents.getUserId().equals(users.getId())) {
            jsonObj.put("error", "Cannot access document of different user");
            logger.info("Cannot access document of different user");
            return new ResponseEntity(jsonObj, HttpStatus.BAD_REQUEST);
        }

        logger.info("Deleting document: " + id);
        String res = storageRepository.deleteFile(documents.getName());
        jsonObj.put("success" ,res);
        logger.info("Successfully Deleted Document: " + id + " from S3 bucket");

        logger.info("Deleting Metadata of document " + id + " from database");
        documentsRepository.delete(documents);
        logger.info("Successfully Deleted Document Data from Database: " + id);

        return new ResponseEntity(jsonObj, HttpStatus.NO_CONTENT);
    }
}
