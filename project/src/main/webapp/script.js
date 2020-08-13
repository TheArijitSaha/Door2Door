/* Constants */
const taskListDivClassName = "taskListDiv";
const taskListUlClassName = "taskListUl";

var taskListCursor = null;
var working = false;

function loadLoginLogoutView() {
  fetch('/login-status').then(response => response.json()).then(status => {
      if (status.loggedIn) {
        loggedInView(status);
        loadTasks();
      } 
      else {
        loggedOutView(status);
      }
  });
}

function loggedInView(status){
  //If the user has incomplete profile, redirect to editProfile.html
  if(status.editDetails){
    window.location.href = 'editProfile.html';
  }

  let loggedinSay = document.createElement('p');
  loggedinSay.innerHTML = 'You are logged in as <strong>';
  loggedinSay.innerHTML += status.email;
  loggedinSay.innerHTML += '</strong>. <a href =\"' + status.logoutUrl + '\">Log out</a>.';

  let myProfile = document.createElement('p');
  myProfile.innerHTML = '<a href ="userProfile.html">My Profile</a>.'

  loggedinSay.appendChild(myProfile);

  document.querySelector('div.' + 'LoginLogoutDiv')
          .appendChild(loggedinSay);
}

function loggedOutView(status){
  let loginAsk = document.createElement('p');
  loginAsk.innerText = 'Sign in !';

  let loginButton = document.createElement('button');
  loginButton.innerHTML = '<a href=\"' + status.loginUrl + '\">Login</a>';

  let loginDiv = document.createElement('div');
  loginDiv.appendChild(loginAsk);
  loginDiv.appendChild(loginButton);

  document.querySelector('div.' + 'LoginLogoutDiv')
          .appendChild(loginDiv);
}

/* 
 * Function to load tasks.
 */
function loadTasks() {
  let fetchURL = '/task/all';
  if (taskListCursor !== null) {
    fetchURL += '?cursor=' + taskListCursor;
  }

  fetch(fetchURL).then(jsonResponse => jsonResponse.json())
                    .then(response => {
    let taskList = document.querySelector('ul.' + taskListUlClassName);
    for (task of response.tasks) {
      let taskEntry = document.createElement('li');
      let taskLink = document.createElement('a');
      taskLink.href = '/task_view.html?taskId=' + task.id;
      taskLink.innerText = task.title;
      taskEntry.appendChild(taskLink);
      taskList.appendChild(taskEntry);
    }
    taskListCursor = response.nextCursor;
    working = false;
  });
}

// For Infinite Scrolling
window.addEventListener('scroll', function() {
  if (document.documentElement.scrollTop + document.documentElement.clientHeight
      >= document.documentElement.scrollHeight) {
    if (working === false) {
      working = true;
      loadTasks();
    }
  }
});

window.onload = () => {
  loadLoginLogoutView();
}

