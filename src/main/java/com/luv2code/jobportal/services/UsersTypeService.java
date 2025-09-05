package com.luv2code.jobportal.services;


import com.luv2code.jobportal.entity.UsersType;
import com.luv2code.jobportal.repository.UsersTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

import static org.antlr.v4.runtime.tree.xpath.XPath.findAll;

@Service
public class UsersTypeService {

    private final UsersTypeRepository usersTypeRepository;

    public UsersTypeService(UsersTypeRepository usersTypeRepository) {
        this.usersTypeRepository = usersTypeRepository;
    }

    public List<UsersType> getAll(){
      return usersTypeRepository.findAll();
    }
}
