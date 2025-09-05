package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entity.RecruiterProfile;
import com.luv2code.jobportal.entity.Users;
import com.luv2code.jobportal.repository.RecruiterProfileRepository;
import com.luv2code.jobportal.repository.UsersRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;
    private final UsersRespository usersRespository;

   @Autowired
    public RecruiterProfileService(RecruiterProfileRepository recruiterProfileRepository, UsersRespository usersRespository) {
        this.recruiterProfileRepository = recruiterProfileRepository;
       this.usersRespository = usersRespository;
   }

    public Optional<RecruiterProfile> getOne(Integer id)
    {
        return recruiterProfileRepository.findById(id);
    }

    public RecruiterProfile addNew(RecruiterProfile recruiterProfile) {

       return recruiterProfileRepository.save(recruiterProfile);
    }

    public RecruiterProfile getCurrentRecruiterProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken))
        {
            String currentUserName = authentication.getName();
            Users users = usersRespository.findByEmail(currentUserName).orElseThrow(()-> new UsernameNotFoundException("user not found"));

            Optional<RecruiterProfile> recruiterProfile = getOne(users.getUserId());
            return recruiterProfile.orElse(null);
        }else return null;
    }
}
