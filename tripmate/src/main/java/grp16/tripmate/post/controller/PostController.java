package grp16.tripmate.post.controller;

import grp16.tripmate.logger.ILogger;
import grp16.tripmate.post.database.IPostDatabase;
import grp16.tripmate.post.database.feedback.IFeedbackDatabase;
import grp16.tripmate.post.model.IPost;
import grp16.tripmate.post.model.Post;
import grp16.tripmate.post.model.PostValidator;
import grp16.tripmate.post.model.factory.IPostFactory;
import grp16.tripmate.post.model.factory.PostFactory;
import grp16.tripmate.post.model.feedback.Feedback;
import grp16.tripmate.session.SessionManager;
import grp16.tripmate.user.database.UserDbColumnNames;
import grp16.tripmate.vehicle.database.VehicleBooking.IVehicleBookingDatabase;
import IVehicleBookingFactory;
import grp16.tripmate.vehicle.model.VehicleBooking.VehicleBookingFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/*
 **References
 **https://www.baeldung.com/spring-thymeleaf-request-parameters
 */

@Controller
public class PostController {
    private final ILogger logger;
    private final IPostFactory postFactory;
    private final IPostDatabase postDatabase;
    private final PostValidator validator;
    private final IFeedbackDatabase feedbackDatabase;
    private final IVehicleBookingFactory vehicleBookingFactory;
    private final IVehicleBookingDatabase vehicleBookingDatabase;

    PostController() {
        postFactory = PostFactory.getInstance();
        logger = postFactory.makeNewLogger(this);
        feedbackDatabase = postFactory.makeFeedbackDatabase();
        postDatabase = postFactory.makePostDatabase();
        validator = postFactory.makePostValidator();
        vehicleBookingFactory = VehicleBookingFactory.getInstance();
        vehicleBookingDatabase = vehicleBookingFactory.getVehicleBookingDatabase();
    }

    @GetMapping("/dashboard")
    public String getAllPosts(Model model) {
        model.addAttribute("title", "Dashboard");
        try {
            IPost post = postFactory.makeNewPost();
            List<Post> posts = post.getAllPosts(postDatabase, SessionManager.getInstance().getLoggedInUserId());
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
        }
        return "listposts";
    }

    @GetMapping("/createpost")
    public String getNewPost(Model model) {
        Post myPost = (Post) postFactory.makeNewPost();
        model.addAttribute("title", "New Post");
        model.addAttribute("post", myPost);
        return "createpost";
    }

    @PostMapping("/createpost")
    public String createPost(Model model, @ModelAttribute Post post) {
        model.addAttribute("title", "Create Post");
        try {
            post.validatePost(validator);
            post.createPost(postDatabase);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
            return "createpost";
        }
    }

    @GetMapping("/myposts")
    public String getUserPosts(Model model) {
        model.addAttribute("title", "My Posts");
        try {
            Post post = (Post) postFactory.makeNewPost();
            List<Post> posts = post.getPostsByUserId(postDatabase, SessionManager.getInstance().getLoggedInUserId());
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            logger.error(e.getMessage());
        }
        return "listposts";
    }

    @GetMapping("/viewpost/{id}")
    public String viewPost(Model model, @PathVariable("id") int postId) {
        model.addAttribute("title", "View Post");
        try {
            Post post = (Post) postFactory.makeNewPost();
            Post myPost = post.getPostByPostId(postDatabase, postId);
            logger.info(myPost.toString());
            model.addAttribute("isUpdateButtonVisible", myPost.getOwner_id() == (int) SessionManager.getInstance().getValue(UserDbColumnNames.ID));
            model.addAttribute("post", myPost);
            model.addAttribute("isFeedbackButtonVisible", myPost.isEligibleForFeedback());
            model.addAttribute("feedbacks", myPost.getFeedbacks(postDatabase, feedbackDatabase));
            model.addAttribute("canJoin", myPost.isEligibleToJoin());
            model.addAttribute("vehicles", myPost.getVehiclesAssociatedWithCurrentPost(postDatabase, vehicleBookingDatabase));

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
        }
        return "viewpost";

    }

    @GetMapping("/editpost/{id}")
    public String editPost(Model model, @PathVariable("id") int postId) {
        model.addAttribute("title", "Edit Post");
        try {
            Post post = (Post) postFactory.makeNewPost();
            Post myPost = post.getPostByPostId(postDatabase, postId);
            model.addAttribute("post", myPost);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
        }
        return "updatepost";
    }

    @PostMapping("/updatepost")
    public String updatePost(Model model, @ModelAttribute Post post) {
        model.addAttribute("title", "Update Post");
        try {
            post.validatePost(validator);
            post.updatePost(postDatabase);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            e.printStackTrace();
        }
        return "updatepost";
    }

    @PostMapping("/deletepost/{id}")
    public String deletePost(Model model, @PathVariable("id") int postId, RedirectAttributes redirectAttrs) {
        model.addAttribute("title", "Delete Post");
        try {
            Post post = (Post) postFactory.makeNewPost();
            Post myPost = post.getPostByPostId(postDatabase, postId);
            myPost.deletePost(postDatabase);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/viewpost/" + postId;
        }
    }

    @PostMapping("/hidepost/{id}")
    public String hidePost(Model model, @PathVariable("id") int postId, RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("title", "Hide Post");
            Post post = (Post) postFactory.makeNewPost();
            Post myPost = post.getPostByPostId(postDatabase, postId);
            myPost.hidePost(postDatabase);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/viewpost/" + postId;
        }
    }

    @GetMapping("/error")
    public String displayError(Model model) {
        model.addAttribute("error", "Some error has occurred");
        return "error";
    }


    @GetMapping("/feedback/{id}")
    public String loadFeedbackPage(Model model, @PathVariable("id") int postId) {
        try {
            model.addAttribute("post", postFactory.makeNewPost().getPostByPostId(postDatabase, postId));
            model.addAttribute("currentFeedback", new Feedback());
            model.addAttribute("title", "Feedback");
        } catch (Exception e) {
            model.addAttribute("error", "Post not found " + e.getMessage());
            e.printStackTrace();
        }
        return "feedback";
    }

    @PostMapping("/feedback/{id}")
    public String createFeedback(@PathVariable("id") int postId, @ModelAttribute Feedback feedback) {
        try {
            feedback.setPostId(postId);
            feedback.setUserId(SessionManager.getInstance().getLoggedInUserId());
            feedback.createFeedback(feedbackDatabase);
        } catch (Exception e) {
            return "redirect:/error";
        }
        return "redirect:/dashboard";
    }
}
