package com.luv2code.jobportal.repository;

import com.luv2code.jobportal.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSeekerProfileReposiotry extends JpaRepository<JobSeekerProfile,Integer> {
}
