package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entity.JobPostActivity;
import com.luv2code.jobportal.entity.JobSeekerProfile;
import com.luv2code.jobportal.entity.JobSeekerSave;
import com.luv2code.jobportal.entity.Users;
import com.luv2code.jobportal.services.JobPostActivityService;
import com.luv2code.jobportal.services.JobSeekerProfileService;
import com.luv2code.jobportal.services.JobSeekerSaveService;
import com.luv2code.jobportal.services.UsersService;
import jakarta.websocket.server.PathParam;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerSaveController {

    private final UsersService usersService;
    private final JobSeekerProfileService jobSeekerProfileService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerSaveService jobSeekerSaveService;


    public JobSeekerSaveController(UsersService usersService, JobSeekerProfileService jobSeekerProfileService, JobPostActivityService jobPostActivityService, JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }
    @PostMapping("/job-details/save/{id}")
    public String save(@PathVariable("id") int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());

            if (seekerProfile.isPresent()) {
                JobSeekerProfile jobSeeker = seekerProfile.get();
                JobPostActivity jobPost = jobPostActivityService.getOne(id);

                // Optional duplicate check
                boolean alreadySaved = jobSeekerSaveService.getCandidatesJob(jobSeeker)
                        .stream()
                        .anyMatch(s -> s.getJob().getJobPostId() == id);
                if (!alreadySaved) {
                    JobSeekerSave newSave = new JobSeekerSave();
                    newSave.setUserId(jobSeeker);
                    newSave.setJob(jobPost);
                    jobSeekerSaveService.addNew(newSave);
                }
            } else {
                throw new RuntimeException("JobSeeker profile not found");
            }
        }

        return "redirect:/dashboard/";
    }

    @GetMapping("saved-jobs/")
    public String saveJobs(Model model)
    {
        List<JobPostActivity> jobPost = new ArrayList<>();
        Object currentUserProfile = usersService.getCurrentUserProfile();

        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);
        for(JobSeekerSave  jobSeekerSave : jobSeekerSaveList)
        {
            jobPost.add(jobSeekerSave.getJob());
        }

        model.addAttribute("jobPost", jobPost);
        model.addAttribute("user",currentUserProfile);

        return "saved-jobs";
    }
}
