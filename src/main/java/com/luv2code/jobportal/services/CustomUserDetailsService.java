package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entity.Users;
import com.luv2code.jobportal.repository.UsersRespository;
import com.luv2code.jobportal.util.CustomUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRespository usersRespository;

    @Autowired
    public CustomUserDetailsService(UsersRespository usersRespository) {
        this.usersRespository = usersRespository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Users user =   usersRespository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("could not found user"));
        return new CustomUserDetail(user);
    }
}
