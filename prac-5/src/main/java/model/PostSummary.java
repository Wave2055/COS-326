package model;

/** Question 4.3d - one post belonging to an account, and whether it was reposted. */
public record PostSummary(String postId, String hashtag, String message, boolean reposted) {
}
