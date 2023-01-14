package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.tag.ReqCreateTagDTO;
import com.example.shinsiri.dtos.req.tag.ReqSearchTagDTO;
import com.example.shinsiri.dtos.res.tag.ResSearchTagDTO;
import com.example.shinsiri.entities.Tag;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public Tag getTagById(int tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        if (tag.isEmpty()) {
            throw new BadRequestException("tagId: " + tagId + " ไม่มีในระบบ");
        }

        return tag.get();
    }

    public ResSearchTagDTO searchTag(ReqSearchTagDTO dto) {
        int pageNo = dto.getPageNo() == null ? 1 : dto.getPageNo();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        String sql = "SELECT * FROM tags WHERE 1=1 ";
        String sqlCount = "SELECT COUNT(id) FROM tags WHERE 1=1 ";
        String sqlCountAll = "SELECT COUNT(id) FROM tags ";

        if (dto.getName() != null && !dto.getName().equals("")) {
            sql += "    AND name LIKE :name ";
            sqlCount += "    AND name LIKE :name ";
        }

        Query query = em.createNativeQuery(sql, Tag.class);
        Query queryCount = em.createNativeQuery(sqlCount);
        Query queryCountAll = em.createNativeQuery(sqlCountAll);

        if (dto.getName() != null && !dto.getName().equals("")) {
            query.setParameter("name", "%" + dto.getName() + "%");
            queryCount.setParameter("name", "%" + dto.getName() + "%");
        }

        query.setFirstResult((pageNo - 1) * pageSize);
        query.setMaxResults(pageSize);

        int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();
        int totalAll = ((BigInteger) queryCountAll.getSingleResult()).intValue();

        ResSearchTagDTO resDto = new ResSearchTagDTO();
        resDto.setPageNo(pageNo);
        resDto.setPageSize(pageSize);
        resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
        resDto.setTotalItems(totalItems);
        resDto.setTotalAll(totalAll);
        resDto.setItems(query.getResultList());

        return resDto;
    }

    public Tag createTag(ReqCreateTagDTO dto) {
        checkTagDuplicate(dto.getName());

        Tag tag = new Tag();
        modelMapper.map(dto, tag);

        tagRepository.saveAndFlush(tag);
        return tag;
    }

    public Tag updateTag(int tagId, ReqCreateTagDTO dto) {
        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag != null) {
            if (!tag.getName().equals(dto.getName())) {
                checkTagDuplicate(dto.getName());

                modelMapper.map(dto, tag);

                tagRepository.saveAndFlush(tag);
            }
        } else {
            throw new BadRequestException("Tag id: " + tagId + " ไม่มีในระบบ");
        }
        return tag;
    }

    public void checkTagDuplicate(String name) {
        Tag tag = tagRepository.findOneByName(name);
        if (tag != null) throw new BadRequestException("Tag มีอยู่แล้วในระบบ");
    }

}
