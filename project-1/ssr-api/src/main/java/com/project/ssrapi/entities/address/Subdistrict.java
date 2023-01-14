package com.project.ssrapi.entities.address;

import javax.persistence.*;

@Entity
@Table(name = "add_subdistricts")
public class Subdistrict {

    private Integer id;
    private Integer districtId;
    private String nameTh;
    private String nameEn;
    private String zipcode;
    private District district;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "district_id")
    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    @Column(name = "name_th")
    public String getNameTh() {
        return nameTh;
    }

    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }

    @Column(name = "name_en")
    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    @Column(name = "zipcode")
    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", insertable = false, updatable = false)
    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }
}
