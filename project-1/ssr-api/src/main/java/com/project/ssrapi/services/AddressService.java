package com.project.ssrapi.services;

import com.example.shinsiri.dtos.res.address.AddressDTO;
import com.example.shinsiri.dtos.res.address.DistrictDTO;
import com.example.shinsiri.dtos.res.address.ProvinceDTO;
import com.example.shinsiri.entities.address.District;
import com.example.shinsiri.entities.address.Province;
import com.example.shinsiri.repositories.address.DistrictRepository;
import com.example.shinsiri.repositories.address.ProvinceRepository;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private DistrictRepository districtRepository;

    public AddressDTO searchAll(String search, int page, int size) {
        String sqlSelectCount = "SELECT COUNT(p.id) ";

        String sqlSelect = "SELECT p.id as provinceid " +
                "   , p.name_th as provinceNameTh " +
                "   , p.name_en as provinceNameEn " +
                "   , d.id as districtId " +
                "   , d.name_th as districtNameTh " +
                "   , d.name_en as districtNameEn " +
                "   , s.id as subdistrictId " +
                "   , s.name_th as subdistrictNameTh " +
                "   , s.name_en as subdistrictNameEn " +
                "   , s.zipcode as zipcode ";

        String sqlFrom = "FROM add_provinces p " +
                "JOIN add_districts d ON d.province_id = p.id " +
                "JOIN add_subdistricts s ON s.district_id = d.id " +
                "WHERE p.name_th LIKE :search " +
                "   OR d.name_th LIKE :search " +
                "   OR s.name_th LIKE :search " +
                "   OR s.zipcode LIKE :search ";

        sqlSelectCount += sqlFrom;
        sqlSelect += sqlFrom + " ORDER BY p.name_th COLLATE \"C\" ";

        Query queryCount = em.createNativeQuery(sqlSelectCount);
        queryCount.setParameter("search", "%" + search + "%");

        int totalResult = ((BigInteger) queryCount.getSingleResult()).intValue();

        Query query = em.createNativeQuery(sqlSelect).unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new AliasToBeanResultTransformer(AddressDTO.Address.class));
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(page * size);

        List<AddressDTO.Address> result = query.getResultList();

        int totalPage = (int) Math.ceil(totalResult / (size + 0.0));

        return new AddressDTO(page, size, totalPage, totalResult, result);
    }

    public AddressDTO searchDistrict(String search, int page, int size) {
        String sqlSelectCount = "SELECT COUNT(p.id) ";

        String sqlSelect = "SELECT p.id as provinceid " +
                "   , p.name_th as provinceNameTh " +
                "   , p.name_en as provinceNameEn " +
                "   , d.id as districtId " +
                "   , d.name_th as districtNameTh " +
                "   , d.name_en as districtNameEn " +
                "   , s.id as subdistrictId " +
                "   , s.name_th as subdistrictNameTh " +
                "   , s.name_en as subdistrictNameEn " +
                "   , s.zipcode as zipcode ";

        String sqlFrom = "FROM add_provinces p " +
                "JOIN add_districts d ON d.province_id = p.id " +
                "JOIN add_subdistricts s ON s.district_id = d.id " +
                "WHERE d.name_th LIKE :search ";

        sqlSelectCount += sqlFrom;
        sqlSelect += sqlFrom + " ORDER BY p.name_th COLLATE \"C\" ";

        Query queryCount = em.createNativeQuery(sqlSelectCount);
        queryCount.setParameter("search", "%" + search + "%");

        int totalResult = ((BigInteger) queryCount.getSingleResult()).intValue();

        Query query = em.createNativeQuery(sqlSelect).unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new AliasToBeanResultTransformer(AddressDTO.Address.class));
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(page * size);

        List<AddressDTO.Address> result = query.getResultList();

        int totalPage = (int) Math.ceil(totalResult / (size + 0.0));

        return new AddressDTO(page, size, totalPage, totalResult, result);
    }

    public List<ProvinceDTO> getProvince(){
       List<Province> list =  provinceRepository.findAll();
       List<ProvinceDTO> result = new ArrayList<>();

       if(list.size()>0){
           for(Province value : list){
               ProvinceDTO provinceDTO = new ProvinceDTO();
               provinceDTO.setId(value.getId());
               provinceDTO.setNameEn(value.getNameEn());
               provinceDTO.setNameTh(value.getNameTh());
               result.add(provinceDTO);
           }
       }

        return result;
    }

    public List<DistrictDTO> getDistrict(int provinceId){
        List<District> list =  districtRepository.findByProvinceId(provinceId);
        List<DistrictDTO> result = new ArrayList<>();

        if(list.size()>0){
            for(District value : list){
                DistrictDTO districtDTO = new DistrictDTO();
                districtDTO.setId(value.getId());
                districtDTO.setNameEn(value.getNameEn());
                districtDTO.setNameTh(value.getNameTh());
                result.add(districtDTO);
            }
        }

        return result;
    }

}
