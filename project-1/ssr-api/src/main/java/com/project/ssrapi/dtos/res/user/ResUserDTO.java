package com.project.ssrapi.dtos.res.user;

import com.example.shinsiri.entities.User;
import com.example.shinsiri.entities.UserImage;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
public class ResUserDTO extends User {

    List<UserImage> userImages;
    boolean isEnabled;
    Collection<SimpleGrantedAuthority> authorities;

}
