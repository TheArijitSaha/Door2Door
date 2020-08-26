package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.sps.data.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet(urlPatterns = {"/profile/*"})
public class ProfileServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // If not logged in, redirect to landing page
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect("/");
      return;
    }

    // Get Logged-in User details
    User loggedInUser = User.getUserFromEmail(userService.getCurrentUser().getEmail());
    if ((loggedInUser == null) || (!loggedInUser.isProfileComplete())) {
      // User is not added in datastore or has not completed profile, redirect to landing page
      response.sendRedirect("/");
      return;
    }

    Long profileUserId = getUserIdFromURI(request.getRequestURI(), loggedInUser);
    if (profileUserId == null) {
      response.getWriter().println("Invalid URI");
      return;
    }

    // Get profile user details
    User profileUser = User.getUserFromId(profileUserId);
    if (profileUser == null) {
      // Given Profile User Id does not exist
      response.getWriter().println("User Id " + profileUserId.toString() + " is invalid");
      return;
    }

    request.setAttribute("profileUser", profileUser);
    request.setAttribute("userLogoutUrl", userService.createLogoutURL("/"));
    request.setAttribute("isMyProfile", (profileUser.getId() == loggedInUser.getId()));
  }

  /**
   * Returns the User Id from the URI.
   * @return The user ID, if a valid Id is there in the URI, else null.
   * @param uri The request URI.
   * @param loggedInUser The logged in user, in case of /profile/me
   */
  private Long getUserIdFromURI(String uri, User loggedInUser) {
    Long userId;

    if (uri.equals("/profile/me")) {
      return loggedInUser.getId();
    }

    try {
      // User ID starts from the string's 9th index
      if (uri.charAt(uri.length() - 1) == '/') {
        taskId = Long.parseLong(uri.substring(9, uri.length() - 1));
      } else {
        taskId = Long.parseLong(uri.substring(9));
      }
    } catch (NumberFormatException nfe) {
      // Given URI does not contain parsable user ID
      return null;
    }

    return userId;
  }

}
