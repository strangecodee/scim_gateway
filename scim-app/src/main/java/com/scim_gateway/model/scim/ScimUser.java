package com.scim_gateway.model.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ScimUser {
    
    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:User");
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("name")
    private Name name;
    
    @JsonProperty("displayName")
    private String displayName;
    
    @JsonProperty("nickName")
    private String nickName;
    
    @JsonProperty("profileUrl")
    private String profileUrl;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("userType")
    private String userType;
    
    @JsonProperty("preferredLanguage")
    private String preferredLanguage;
    
    @JsonProperty("locale")
    private String locale;
    
    @JsonProperty("timezone")
    private String timezone;
    
    @JsonProperty("active")
    private boolean active = true;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("emails")
    private List<Email> emails;
    
    @JsonProperty("phoneNumbers")
    private List<PhoneNumber> phoneNumbers;
    
    @JsonProperty("addresses")
    private List<Address> addresses;
    
    @JsonProperty("photos")
    private List<Photo> photos;
    
    @JsonProperty("groups")
    private List<GroupRef> groups;
    
    @JsonProperty("entitlements")
    private List<Entitlement> entitlements;
    
    @JsonProperty("roles")
    private List<Role> roles;
    
    @JsonProperty("x509Certificates")
    private List<X509Certificate> x509Certificates;
    
    @JsonProperty("meta")
    private Meta meta;
    
    // Embedded classes
    @Data
    public static class Name {
        @JsonProperty("formatted")
        private String formatted;
        
        @JsonProperty("familyName")
        private String familyName;
        
        @JsonProperty("givenName")
        private String givenName;
        
        @JsonProperty("middleName")
        private String middleName;
        
        @JsonProperty("honorificPrefix")
        private String honorificPrefix;
        
        @JsonProperty("honorificSuffix")
        private String honorificSuffix;
    }
    
    @Data
    public static class Email {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("display")
        private String display;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("primary")
        private boolean primary;
    }
    
    @Data
    public static class PhoneNumber {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("primary")
        private boolean primary;
    }
    
    @Data
    public static class Address {
        @JsonProperty("formatted")
        private String formatted;
        
        @JsonProperty("streetAddress")
        private String streetAddress;
        
        @JsonProperty("locality")
        private String locality;
        
        @JsonProperty("region")
        private String region;
        
        @JsonProperty("postalCode")
        private String postalCode;
        
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("type")
        private String type;
    }
    
    @Data
    public static class Photo {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("type")
        private String type;
    }
    
    @Data
    public static class GroupRef {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("$ref")
        private String ref;
        
        @JsonProperty("display")
        private String display;
    }
    
    @Data
    public static class Entitlement {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("display")
        private String display;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("primary")
        private boolean primary;
    }
    
    @Data
    public static class Role {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("display")
        private String display;
        
        @JsonProperty("type")
        private String type;
    }
    
    @Data
    public static class X509Certificate {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("display")
        private String display;
        
        @JsonProperty("type")
        private String type;
    }
    
    @Data
    public static class Meta {
        @JsonProperty("resourceType")
        private String resourceType;
        
        @JsonProperty("created")
        private String created;
        
        @JsonProperty("lastModified")
        private String lastModified;
        
        @JsonProperty("location")
        private String location;
        
        @JsonProperty("version")
        private String version;
    }
}
