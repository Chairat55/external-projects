package com.project.ssrapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "customers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class Customer implements Serializable{

    private Integer id;
    private String type;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_date")
    private Date businessDate;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "address")
    private String address;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "subdistrict")
    private String subdistrict;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "contact_tel")
    private String contactTel;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_detail")
    private String contactDetail;

    @Column(name = "user_id")
    private int userId;

    @CreatedBy
    @Basic
    @Column(name = "created_by")
    private String createdBy;
    @LastModifiedBy
    @Basic
    @Column(name = "updated_by")
    private String updatedBy;
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    @Column(name = "created_date")
    private Date createdDate;
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    @Column(name = "updated_date")
    private Date updatedDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContactTel() {
        if (contactTel != null) {
            if (contactTel.length() == 9) {
                return contactTel.substring(0, 2) + "-" + contactTel.substring(2, 5) + "-" + contactTel.substring(5, 9);

            } else if (contactTel.length() == 10) {
                return contactTel.substring(0, 3) + "-" + contactTel.substring(3, 6) + "-" + contactTel.substring(6, 10);

            } else {
                return contactTel;
            }
        }
        return "";
    }
}
