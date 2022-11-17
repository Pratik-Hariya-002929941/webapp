package com.example.neu.springbootapp.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.neu.springbootapp.model.OneTimeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OneTimeTokenRepository {

    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    public OneTimeTokenRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public OneTimeToken getOneTimeToken(String email, String token) {
        return dynamoDBMapper.load(OneTimeToken.class, email, token);
    }

    public OneTimeToken createOneTimeToken(OneTimeToken oneTimeToken) {
        dynamoDBMapper.save(oneTimeToken);
        return oneTimeToken;
    }
}
