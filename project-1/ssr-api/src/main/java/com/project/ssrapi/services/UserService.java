package com.project.ssrapi.services;

import com.example.shinsiri.dtos.req.user.ReqCreateUserDTO;
import com.example.shinsiri.dtos.req.user.ReqSearchUserDTO;
import com.example.shinsiri.dtos.res.user.ResSearchUserDTO;
import com.example.shinsiri.dtos.res.user.ResUserDTO;
import com.example.shinsiri.entities.User;
import com.example.shinsiri.entities.UserRole;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.UserImageRepository;
import com.example.shinsiri.repositories.UserRepository;
import com.example.shinsiri.repositories.UserRoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private UserImageRepository userImageRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager em;

    public ResUserDTO getUserById(int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new BadRequestException("userId: " + userId + " ไม่มีในระบบ");
        }

        ResUserDTO resDto = new ResUserDTO();
        modelMapper.map(user.get(), resDto);

        resDto.setUserImages(userImageRepository.findAllByUserId(userId));
        return resDto;
    }

    public ResSearchUserDTO searchUser(ReqSearchUserDTO dto) {
        int pageNo = dto.getPageNo() == null ? 1 : dto.getPageNo();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();

        String sql = "SELECT u.* " +
                "FROM users u " +
                "JOIN user_role ur ON u.id = ur.user_id " +
                "JOIN role r ON ur.role_id = r.id " +
                "WHERE 1=1 ";
        String sqlCount = "SELECT COUNT(u.id) " +
                "FROM users u " +
                "JOIN user_role ur ON u.id = ur.user_id " +
                "JOIN role r ON ur.role_id = r.id " +
                "WHERE 1=1 ";
        String sqlCountAll = "SELECT COUNT(u.id) " +
                "FROM users u " +
                "JOIN user_role ur ON u.id = ur.user_id " +
                "JOIN role r ON ur.role_id = r.id ";

        if (dto.getType() != null && !dto.getType().equals("")) {
            sql += "    AND r.name = :type ";
            sqlCount += "    AND r.name = :type ";
        }

        Query query = em.createNativeQuery(sql, User.class);
        Query queryCount = em.createNativeQuery(sqlCount);
        Query queryCountAll = em.createNativeQuery(sqlCountAll);

        if (dto.getType() != null && !dto.getType().equals("")) {
            query.setParameter("type", dto.getType());
            queryCount.setParameter("type", dto.getType());
        }

        query.setFirstResult((pageNo - 1) * pageSize);
        query.setMaxResults(pageSize);

        int totalItems = ((BigInteger) queryCount.getSingleResult()).intValue();
        int totalAll = ((BigInteger) queryCountAll.getSingleResult()).intValue();

        ResSearchUserDTO resDto = new ResSearchUserDTO();
        resDto.setPageNo(pageNo);
        resDto.setPageSize(pageSize);
        resDto.setTotalPages((int) Math.ceil(totalItems / (pageSize + 0.0)));
        resDto.setTotalItems(totalItems);
        resDto.setTotalAll(totalAll);
        resDto.setItems(query.getResultList());

        return resDto;
    }

    public User createUser(ReqCreateUserDTO dto) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String bcryptPassword = encoder.encode(dto.getPassword());

        User user = new User();
        modelMapper.map(dto, user);

        user.setPassword("{bcrypt}" + bcryptPassword);
        user.setRawPassword(dto.getPassword());
        user.setEnabled(true);
        user.setNonLocked(true);
        userRepository.saveAndFlush(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(dto.getRoleId());
        userRoleRepository.saveAndFlush(userRole);

        return user;
    }

    public User updateUser(int userId, ReqCreateUserDTO dto) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String bcryptPassword = encoder.encode(dto.getPassword());

            modelMapper.map(dto, user);

            user.setPassword("{bcrypt}" + bcryptPassword);
            user.setRawPassword(dto.getPassword());
            user.setEnabled(true);
            user.setNonLocked(true);

            if (!user.getUserRole().getRoleId().equals(dto.getRoleId())) {
                user.getUserRole().setRoleId(dto.getRoleId());
            }

            userRepository.saveAndFlush(user);

        } else {
            throw new BadRequestException("User id: " + userId + " ไม่มีในระบบ");
        }

        return user;
    }

    public Map<String, String> deleteUserById(int userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        } else {
            throw new BadRequestException("User id: " + userId + " ไม่มีในระบบ");
        }
        return new HashMap<>() {{
            put("Message", "ลบสำเร็จ");
        }};
    }

}
