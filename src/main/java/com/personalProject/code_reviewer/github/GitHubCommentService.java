package com.personalProject.code_reviewer.github;

import com.personalProject.code_reviewer.github.model.GitHubReviewComment;
import com.personalProject.code_reviewer.github.model.GitHubReviewRequest;
import com.personalProject.code_reviewer.llm.model.ReviewComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
public class GitHubCommentService {

    private final RestClient restClient;

    public GitHubCommentService(RestClient githubRestClient){
        this.restClient=githubRestClient;
    }

    public void postReview(String repoFullName,int prNumber, List<ReviewComment> comments){

        try{
            log.atDebug().log("GitHubCommentService.postReview() - start");
            String[] parts = repoFullName.split("/");
            String owner = parts[0];
            String repo  = parts[1];
            log.atDebug().addArgument(owner).addArgument(repo).log("GitHubCommentService.postReview() - owner:{}, repo:{}");

            String summaryBody = String.format(
                    "Automated code review by LLM. Found %d suggestion(s). Severity breakdown: %d ERROR, %d WARN, %d INFO.",
                    comments.size(),
                    comments.stream().filter(c -> "ERROR".equalsIgnoreCase(c.severity())).count(),
                    comments.stream().filter(c -> "WARN".equalsIgnoreCase(c.severity())).count(),
                    comments.stream().filter(c -> "INFO".equalsIgnoreCase(c.severity())).count()
            );
            log.atDebug().addArgument(summaryBody).log("GitHubCommentService.postReview() - summaryBody:{}");
            List<GitHubReviewComment> githubComments = comments.stream()
                    .map(c -> new GitHubReviewComment(
                            c.file(),
                            String.format("[%s] %s", c.severity(), c.suggestion()),
                            1
                    ))
                    .toList();
            log.atDebug().addArgument(githubComments.size()).log("GitHubCommentService.postReview() - Number of GithubComments:{}");
            boolean prHasErrors= comments.stream()
                    .anyMatch(comment -> "ERROR".equalsIgnoreCase(comment.severity()));
            String reviewEvent= prHasErrors ? "REQUEST_CHANGES" : "COMMENT";
            log.atDebug().addArgument(prHasErrors).addArgument(reviewEvent).log("GitHubCommentService.postReview() - HasErrors:{},Review Event:{}");
            GitHubReviewRequest reviewRequest = new GitHubReviewRequest(
                    summaryBody,
                    reviewEvent,
                    githubComments
            );

            String url = String.format(
                    "https://api.github.com/repos/%s/%s/pulls/%d/reviews",
                    owner, repo, prNumber
            );
            log.atDebug().addArgument(url).log("GitHubCommentService.postReview() - URL:{}");
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reviewRequest)
                    .retrieve()
                    .toBodilessEntity();
            log.atDebug().log("GitHubCommentService.postReview() - end ");
        } catch (RestClientException e) {
            log.error("Failed to post review to GitHub for PR #{}: {}",
                    prNumber, e.getMessage());
        }

    }

}
