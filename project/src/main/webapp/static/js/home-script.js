/* Constants */
const taskListDivClassName = "taskListDiv";

var taskListCursor = null;
var working = false;

function constructTaskNode(task) {
  let deadline = new Date(task.deadline);
  let deadlineString = deadline.toLocaleString('en-US',
      { hour: 'numeric', minute: 'numeric', hour12: true });
  deadlineString += ', ' + deadline.toDateString();

  let f = '<div class="task-node">' +
        '<div class="task-first-line">' +
          '<a href="/task/view/' + task.id + '"><span class="task-title">' + task.title + '</span></a>' +
          '<span class="task-compensation">&#x20B9; ' + task.compensation + '</span>' +
        '</div>' +

        '<div class="task-second-line">' +
          '<span class="task-deadline">Deadline: ' + deadlineString + '</span>' +
        '</div>' +
      '</div>';

  return f;
}

/*
 * Function to load tasks.
 */
function loadTasks(refreshList = false) {
  if (working) return;
  working = true;

  let fetchURL = '/task/all';
  let taskList = $('div.' + taskListDivClassName);

  // If the list is to be refreshed, then cursor should be null
  if (refreshList) {
    taskListCursor = null;
    taskList.empty();
  }

  let sortOption = $('#taskSortOption').val();
  let sortDirection = $('#taskSortDirection').val();
  let filterActive = $('#taskFilterActive').prop('checked');

  $.get(fetchURL, { cursor: taskListCursor,
                    sortOption: sortOption,
                    sortDirection: sortDirection,
                    filterActive: filterActive
      }).done(function(response) {

    if (refreshList) {
      // Clear again as any parallel request might have populated the list again
      taskList.empty()
    }

    // Append Tasks to list
    for (task of response.tasks) {
      taskNode = $(constructTaskNode(task));
      taskList.append(taskNode);
    }

    // Update cursor
    taskListCursor = response.nextCursor;
    working = false;
  });
}

// For Infinite Scrolling
window.addEventListener('scroll', function() {
  if (document.documentElement.scrollTop + document.documentElement.clientHeight
      >= document.documentElement.scrollHeight) {
    loadTasks();
  }
});

$(document).ready(function(){
  loadTasks();

  // For Switching Sort Options
  $('#taskSortOption').on('change',function(){
    loadTasks(true);
  });

  // For Switching Sort Directions
  $('#taskSortDirection').on('change',function(){
    loadTasks(true);
  });

  // For Switching Active Filter
  $('#taskFilterActive').on('change',function(){
    loadTasks(true);
  });

});
