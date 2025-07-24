package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entity.JobSeekerProfile;
import com.luv2code.jobportal.repository.JobSeekerProfileReposiotry;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JobSeekerProfileService {

    private JobSeekerProfileReposiotry jobSeekerProfileReposiotry;

    public JobSeekerProfileService(JobSeekerProfileReposiotry jobSeekerProfileReposiotry) {
        this.jobSeekerProfileReposiotry = jobSeekerProfileReposiotry;
    }

    public Optional<JobSeekerProfile> getOne(Integer id) {
        return jobSeekerProfileReposiotry.findById(id);
    }
}
