package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entity.*;
import com.luv2code.jobportal.services.JobPostActivityService;
import com.luv2code.jobportal.services.JobSeekerApplyService;
import com.luv2code.jobportal.services.JobSeekerSaveService;
import com.luv2code.jobportal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

@Controller
public class jobPostActivityController {

    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public jobPostActivityController(UsersService usersService,
                                     JobPostActivityService jobPostActivityService,
                                     JobSeekerApplyService jobSeekerApplyService,
                                     JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("/dashboard/")
    public String searchJob(Model model,
                            @RequestParam(value = "job", required = false) String job,
                            @RequestParam(value = "location", required = false) String location,
                            @RequestParam(value = "partTime", required = false) String partTime,
                            @RequestParam(value = "fullTime", required = false) String fullTime,
                            @RequestParam(value = "freelance", required = false) String freelance,
                            @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                            @RequestParam(value = "officeOnly", required = false) String officeOnly,
                            @RequestParam(value = "partialRemote", required = false) String partialRemote,
                            @RequestParam(value = "today", required = false) Boolean today,
                            @RequestParam(value = "days7", required = false) Boolean days7,
                            @RequestParam(value = "days30", required = false) Boolean days30) {

        // Add checkbox states to model
        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));
        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));
        model.addAttribute("today", Boolean.TRUE.equals(today));
        model.addAttribute("days7", Boolean.TRUE.equals(days7));
        model.addAttribute("days30", Boolean.TRUE.equals(days30));
        model.addAttribute("job", job);
        model.addAttribute("location", location);

        // Determine search date
        LocalDate searchDate = null;
        boolean dateSearchFlag = true;

        if (Boolean.TRUE.equals(days30)) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (Boolean.TRUE.equals(days7)) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (Boolean.TRUE.equals(today)) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }

        // Employment Type fallback
        boolean typeSkipped = false;
        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            typeSkipped = true;
        }

        // Remote Type fallback
        boolean remoteSkipped = false;
        if (officeOnly == null && remoteOnly == null && partialRemote == null) {
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            remoteSkipped = true;
        }

        // Final search
        List<JobPostActivity> jobPost;
        if (!dateSearchFlag && typeSkipped && remoteSkipped &&
                !StringUtils.hasText(job) && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
        } else {
            jobPost = jobPostActivityService.search(
                    job,
                    location,
                    Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote),
                    searchDate
            );
        }

        // Get current logged-in user info
        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);

            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                List<RecruiterJobDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(
                        ((RecruiterProfile) currentUserProfile).getUserAccountId());
                model.addAttribute("jobPost", recruiterJobs);
            } else {
                List<JobSeekerApply> appliedJobs = jobSeekerApplyService.getCandidatesJobs((JobSeekerProfile) currentUserProfile);
                List<JobSeekerSave> savedJobs = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

                for (JobPostActivity jobActivity : jobPost) {
                    boolean isApplied = appliedJobs.stream()
                            .anyMatch(apply -> Objects.equals(apply.getJob().getJobPostId(), jobActivity.getJobPostId()));
                    boolean isSaved = savedJobs.stream()
                            .anyMatch(save -> Objects.equals(save.getJob().getJobPostId(), jobActivity.getJobPostId()));

                    jobActivity.setIsActive(isApplied);
                    jobActivity.setIsSaved(isSaved);
                }

                model.addAttribute("jobPost", jobPost);
            }
        }

        model.addAttribute("user", currentUserProfile);
        return "dashboard";
    }

    @GetMapping("global-search/")
    public String globalSearch(
            Model model,
            @RequestParam(value = "job", required = false) String job,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "partTime", required = false) String partTime,
            @RequestParam(value = "fullTime", required = false) String fullTime,
            @RequestParam(value = "freelance", required = false) String freelance,
            @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
            @RequestParam(value = "officeOnly", required = false) String officeOnly,
            @RequestParam(value = "partialRemote", required = false) String partialRemote,
            @RequestParam(value = "today", required = false) Boolean today,
            @RequestParam(value = "days7", required = false) Boolean days7,
            @RequestParam(value = "days30", required = false) Boolean days30
    ){

        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));
        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));
        model.addAttribute("today", Boolean.TRUE.equals(today));
        model.addAttribute("days7", Boolean.TRUE.equals(days7));
        model.addAttribute("days30", Boolean.TRUE.equals(days30));
        model.addAttribute("job", job);
        model.addAttribute("location", location);


        LocalDate searchDate = null;
        boolean dateSearchFlag = true;

        if (Boolean.TRUE.equals(days30)) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (Boolean.TRUE.equals(days7)) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (Boolean.TRUE.equals(today)) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }

        // Employment Type fallback
        boolean typeSkipped = false;
        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            typeSkipped = true;
        }

        // Remote Type fallback
        boolean remoteSkipped = false;
        if (officeOnly == null && remoteOnly == null && partialRemote == null) {
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            remoteSkipped = true;
        }

        // Final search
        List<JobPostActivity> jobPost;
        if (!dateSearchFlag && typeSkipped && remoteSkipped &&
                !StringUtils.hasText(job) && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
        } else {
            jobPost = jobPostActivityService.search(
                    job,
                    location,
                    Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote),
                    searchDate
            );
        }

        model.addAttribute("jobPost", jobPost);

        return "global-search";

    }

    @GetMapping("/dashboard/add")
    public String addJobs(Model model) {
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model model) {
        Users user = usersService.getCurrentUser();
        if (user != null) {
            jobPostActivity.setPostedById(user);
        }
        jobPostActivity.setPostedDate(new Date());
        model.addAttribute("jobPostActivity", jobPostActivity);
        jobPostActivityService.addnew(jobPostActivity);
        return "redirect:/dashboard/";
    }

    @PostMapping("/dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model) {
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);
        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }
}
