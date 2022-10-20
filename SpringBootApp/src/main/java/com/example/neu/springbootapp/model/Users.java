package com.example.neu.springbootapp.model;

import com.example.neu.springbootapp.Annotation.DateReadOnly;
import com.example.neu.springbootapp.Annotation.ReadOnly;
import com.example.neu.springbootapp.Annotation.UniqueEmail;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="users")
public class Users {


//    @ReadOnly
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    @NotNull(message = "First name cannot be null")
    @NotEmpty(message = "First name cannot be empty")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull(message = "First name cannot be null")
    @NotEmpty(message = "First name cannot be empty")
    @Column(name= "last_name", nullable = false)
    private String lastName;

    @Email
    @NotNull(message = "Username shouldn't be null")
    @NotEmpty(message = "Username shouldn't be empty")
//    @UniqueEmail
    @Column(name = "username", nullable = false)
    private String username;


    @NotNull(message = "Password cannot be null")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name="password", nullable = false)
    private String password;

//    @DateReadOnly
    @CreationTimestamp
    @Column(name="account_created", nullable = false, updatable = false)
    private Date accountCreated;

//    @DateReadOnly
    @UpdateTimestamp
    @Column(name="account_update")
    private Date accountUpdated;

    public Users() {
    }

    public Users(UUID id, String firstName, String lastName, String username, String password, Date accountCreated, Date accountUpdated) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.accountCreated = accountCreated;
        this.accountUpdated = accountUpdated;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Date getAccountCreated() {
        return accountCreated;
    }

    public void setAccountCreated(Date accountCreated) {
        this.accountCreated = accountCreated;
    }

    public Date getAccountUpdated() {
        return accountUpdated;
    }

    public void setAccountUpdated(Date accountUpdated) {
        this.accountUpdated = accountUpdated;
    }


}
