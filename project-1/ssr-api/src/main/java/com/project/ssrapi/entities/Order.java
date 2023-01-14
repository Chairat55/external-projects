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
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class Order implements Serializable{

    private Integer id;

    @Column(name = "customer_id")
    private int customerId;

    @Column(name = "userId")
    private int userId;

    @Column(name = "priority")
    private int priority;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_tel")
    private String contactTel;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "delivery_province")
    private String deliveryProvince;

    @Column(name = "delivery_district")
    private String deliveryDistrict;

    @Column(name = "delivery_subdistrict")
    private String deliverySubdistrict;

    @Column(name = "delivery_zipcode")
    private String deliveryZipcode;

    @Column(name = "total")
    private double total;

    @Column(name = "discount")
    private double discount;

    @Column(name = "net")
    private double net;

    @Column(name = "payment_term")
    private String paymentTerm;

    @Column(name = "payment_credit")
    private String paymentCredit;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "remark")
    private String remark;

    @Column(name = "status")
    private String status;

    @Column(name = "reject_remark")
    private String rejectRemark;

    @Column(name = "reject_due_date")
    private LocalDateTime rejectDueDate;

    @Column(name = "delivery_type")
    private String deliveryType;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

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
