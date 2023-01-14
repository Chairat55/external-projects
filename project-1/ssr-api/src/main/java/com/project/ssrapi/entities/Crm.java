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
@Table(name = "crms")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class Crm implements Serializable{

    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "business_date")
    private Date businessDate;

    @Column(name = "hashtags")
    private String hashtags;

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

    @Column(name = "contact_tel")
    private String contactTel;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_detail")
    private String contactDetail;

    @Column(name = "expect_result")
    private String expectResult;

    @Column(name = "expect_result_detail")
    private String expectResultDetail;

    @Column(name = "expect_result_date")
    private Date expectResultDate;

    @Column(name = "expect_result_time")
    private String expectResultTime;

    @Column(name = "actual_result")
    private String actualResult;

    @Column(name = "actual_result_detail")
    private String actualResultDetail;

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

}
