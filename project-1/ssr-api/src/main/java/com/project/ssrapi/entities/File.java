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
@Table(name = "files")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class File implements Serializable{

    private Integer id;
    private String name;
    private String type;
    private String filePath;
    private String fileName;

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
