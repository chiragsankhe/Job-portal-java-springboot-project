package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entity.*;
import com.luv2code.jobportal.repository.JobPostActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobPostActivityService {

    private final JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    public JobPostActivityService(JobPostActivityRepository jobPostActivityRepository) {
        this.jobPostActivityRepository = jobPostActivityRepository;
    }

    public JobPostActivity addNew(JobPostActivity jobPostActivity)
    {
        return jobPostActivityRepository.save(jobPostActivity);
    }
    public List<RecruiterJobDto> getRecruiterJobs(int recruiter)
    {
       List<IRecruiterJobs>  recruiterJobsDtos =  jobPostActivityRepository.getRecruiterJobs(recruiter);
       List<RecruiterJobDto> recruiterJobsDtoList = new ArrayList<>();

       for(IRecruiterJobs rec: recruiterJobsDtos)
       {
           JobLocation loc = new JobLocation(rec.getLocationId(), rec.getCity() , rec.getState() , rec.getCountry());
           JobCompany comp= new JobCompany(rec.getCompanyId(), rec.getName(), "");

           recruiterJobsDtoList.add(new RecruiterJobDto(rec.getTotalCandidates(), rec.getJob_post_id(),
                   rec.getJob_title(), loc, comp));
       }

       return recruiterJobsDtoList;
    }

    public JobPostActivity getOne(int id) {

        return jobPostActivityRepository.findById(id).orElseThrow(() ->new RuntimeException("Job not found"));
    }
}
