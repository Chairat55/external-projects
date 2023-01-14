package com.project.ssrapi.dtos.res.address;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AddressDTO {
    private int page;
    private int size;
    private int totalPage;
    private int totalResult;
    private List<Address> result;

    public AddressDTO() {
    }

    public AddressDTO(int page, int size, int totalPage, int totalResult, List<Address> result) {
        this.page = page;
        this.size = size;
        this.totalPage = totalPage;
        this.totalResult = totalResult;
        this.result = result;
    }

    public static class Address {
        @JsonProperty("provinceId")
        private Integer provinceid;
        @JsonProperty("provinceNameTh")
        private String provincenameth;
        @JsonProperty("provinceNameEn")
        private String provincenameen;
        @JsonProperty("districtId")
        private Integer districtid;
        @JsonProperty("districtNameTh")
        private String districtnameth;
        @JsonProperty("districtNameEn")
        private String districtnameen;
        @JsonProperty("subdistrictId")
        private Integer subdistrictid;
        @JsonProperty("subdistrictNameTh")
        private String subdistrictnameth;
        @JsonProperty("subdistrictNameEn")
        private String subdistrictnameen;
        @JsonProperty("zipcode")
        private String zipcode;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public List<Address> getResult() {
        return result;
    }

    public void setResult(List<Address> result) {
        this.result = result;
    }
}
