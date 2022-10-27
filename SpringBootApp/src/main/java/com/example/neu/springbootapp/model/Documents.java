package com.example.neu.springbootapp.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="documents")
public class Documents {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "name cannot be null")
    @NotEmpty(message = "name cannot be empty")
    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name="date_created", nullable = false, updatable = false)
    private Date dateCreated;

    @NotNull(message = "S3 bucket name cannot be null")
    @NotEmpty(message = "S3 bucket name cannot be empty")
    @Column(name = "s3_bucket_path", nullable = false)
    private String s3_bucket_path;

    public Documents(UUID id, UUID userId, String name, Date dateCreated, String s3_bucket_path) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.dateCreated = dateCreated;
        this.s3_bucket_path = s3_bucket_path;
    }

    public Documents() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID user_id) {
        this.userId = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }
}
