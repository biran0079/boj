
function setCookie(sName,sValue, expiresAt) {
    var date;
    if (expiresAt) {
        date = new Date(expiresAt)
    } else {
        date = new Date();
        date.setMonth(date.getMonth() + 1);
    }
    var sCookie = encodeURIComponent(sName) + '=' + encodeURIComponent(sValue) + ';expires=' + date.toGMTString() + ';path=/';
    document.cookie = sCookie;
}

function deleteCookie(name) {
  document.cookie = name +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) {
    return parts.pop().split(";").shift();
  }
}

var onSignIn = function (googleUser) {
  console.log("signed in");
  var authResp = googleUser.getAuthResponse();
  var idToken = authResp.id_token;
  var expiresAt = authResp.expires_at;
  if (getCookie('id_token') != idToken) {
      setCookie('id_token', idToken, expiresAt);
      location.reload();
  }
}

function onLoad() {
    gapi.signin2.render('g-signin', {
        'scope': 'profile email',
        'width': 208,
        'height': 45,
        'longtitle': true,
        'theme': 'light',
        'onsuccess': onSignIn,
        'onfailure': function (error) {
            console.log(error);
        }
    });
}

function signOut() {
    deleteCookie('id_token');
    gapi.load('auth2', function() {
        var auth2 = gapi.auth2.init();
        auth2.then(function () {
            auth2.signOut().then(function() {
                console.log('signed out')
                location.href = '/';
            });
        });
    });
}

function deleteProblem(id) {
    $.ajax({
        url: '/problem/' + id,
        type: 'DELETE',
        success: function(result) {
            location.reload();
        }
    });
}

function deleteRoster(id) {
    $.ajax({
        url: '/roster/' + id,
        type: 'DELETE',
        success: function(result) {
            location.reload();
        }
    });
}

function initEditor(elem) {
    editor = CodeMirror.fromTextArea(elem, {
        lineNumbers: true,
        mode: "text/x-java",
        matchBrackets: true,
        tabSize: 2,
        extraKeys: {
            "Tab": function(cm){
                cm.replaceSelection("  " , "end");
            }
        }
    });
    editor.setSize('100%', '100%');
    return editor;
}

$(document).ready(function() {
    $('select').material_select();
});