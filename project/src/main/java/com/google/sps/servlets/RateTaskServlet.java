package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Task;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.text.ParseException;

/** Servlet facilitating assign task. */
@WebServlet("/task/rate")
public class RateTaskServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long taskId = getParameter(request, "taskId", -1);
    if (taskId == -1)
      return;

    long assigneeId = getParameter(request, "assigneeId", -1);
    System.out.println(taskId + " " + assigneeId);
    float completionRating = Float.parseFloat(request.getParameter("rating"));
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      return;
    }
    
    Entity taskEntity;
    try{
      taskEntity = datastore.get(KeyFactory.createKey("Task", taskId));
    }catch (Exception e) {
      System.out.println(e);
      return;
    }
    setRatingForUser(getNumberOfTasksCompleted(assigneeId), assigneeId, completionRating);
    
    taskEntity.setProperty("completionRating", completionRating);
    taskEntity.setProperty("active", false);
    datastore.put(taskEntity);
    response.sendRedirect("/task-view.html?taskId=" + String.valueOf(taskId));
  }

  private long getParameter(HttpServletRequest request, String name, long defaultValue) {
    Long value = Long.parseLong(request.getParameter(name));
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  private int getNumberOfTasksCompleted(long assigneeId){
    Query query = new Query("Task");
    Filter assigneeFilter = new FilterPredicate("assigneeId", FilterOperator.EQUAL, assigneeId);
    query.setFilter(assigneeFilter);

    /*
    TODO : Add another attribute to User, that tracks the number of tasks compeleted 
            by that particular User
    */
    int count = 0;
    PreparedQuery pq = datastore.prepare(query);
    for(Entity taskEntity : pq.asIterable()){
      boolean active = Boolean.parseBoolean(taskEntity.getProperty("active").toString());
      if(!active){
        count++;
      }
    }

    System.out.println("Count " + count);

    return count;
  }

  private void setRatingForUser(int count, long assigneeId, float completionRating){
    Entity userEntity;
    try{
      userEntity = datastore.get(KeyFactory.createKey("User", assigneeId));
    }catch (Exception e) {
      System.out.println(e);
      return;
    }

    float currRating = Float.parseFloat(userEntity.getProperty("rating").toString());

    float newRating = (currRating * count + completionRating) / (count + 1);
    System.out.println("Count " + count);
    System.out.println("Old Rating " + currRating);
    System.out.println("New Rating " + newRating);
    userEntity.setProperty("rating", newRating);
    datastore.put(userEntity);
  }
}
