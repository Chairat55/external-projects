package com.project.ssrapi.entities.address;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "add_districts")
public class District {

    private Integer id;
    private Integer provinceId;
    private String nameTh;
    private String nameEn;
    private Province province;
    private List<Subdistrict> subdistricts;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "province_id")
    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", insertable = false, updatable = false)
    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL )
    public List<Subdistrict> getSubdistricts() {
        return subdistricts;
    }

    public void setSubdistricts(List<Subdistrict> subdistricts) {
        this.subdistricts = subdistricts;
    }
}
