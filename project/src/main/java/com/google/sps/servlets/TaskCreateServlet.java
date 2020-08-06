package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
// import com.google.gson.Gson;
import com.google.sps.data.Task;
import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/** Servlet facilitating creation of tasks. */
@WebServlet("/task/create")
public class TaskCreateServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    request.getRequestDispatcher("/task_create.html").forward(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* TODO: Add user service part after completion of login pipeline
    UserService userService = UserServiceFactory.getUserService();
    
    // If not logged in, do not create new task
    if (!userService.isUserLoggedIn()) {
      return;
    }

    // Get User details
    String userEmail = userService.getCurrentUser().getEmail();
    */

    /*// Get the input from the form.
    String commentText = getParameter(request, "comment", "");*/

    // TODO: add checks to task details coming from client

    // Form Entity
    Entity taskEntity = new Entity("Task");
    Task task = new Task(taskEntity.getKey().getId(),
      getParameter(request, "title", ""),
      getParameter(request, "details", ""),
      Long.parseLong(getParameter(request, "compensation", "0")),
      -1, /* TODO: Creator Id: update after login pipeline is complete */
      getParameter(request, "address", "")
    );
    if (!setTaskEntityProperties(task, taskEntity)) {
      return;
    }

    // Store in Datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    // TODO: Redirect to Task View after creation of task to view the created task
    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * If present, get the request parameter identified by name, else return defaultValue.
   *
   * @param request The HTTP Servlet Request.
   * @param name The name of the rquest parameter.
   * @param defaultValue The default value to be returned if required parameter is unspecified.
   * @return The request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  private boolean setTaskEntityProperties (Task task, Entity taskEntity) {
    if ((!taskEntity.getKind().equals("Task"))
        || (taskEntity.getKey().getId() != task.getId())) {
      return false;
    }

    taskEntity.setProperty("title", task.getTitle());
    taskEntity.setProperty("details", task.getDetails());
    taskEntity.setProperty("creationTime", task.getCreationTime());
    taskEntity.setProperty("compensation", task.getCompensation());
    taskEntity.setProperty("creatorId", task.getCreatorId());
    taskEntity.setProperty("address", task.getAddress());
    taskEntity.setProperty("assigned", task.isAssigned());
    taskEntity.setProperty("assigneeId", task.getAssigneeId());
    taskEntity.setProperty("completionRating", task.getCompletionRating());
    taskEntity.setProperty("active", task.isActive());
    return true;
  }
}

