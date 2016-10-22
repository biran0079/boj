
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

function delete_cookie(name) {
  document.cookie = name +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

var onSignIn = function (googleUser) {
  console.log("signed in");
  var authResp = googleUser.getAuthResponse();
  var idToken = authResp.id_token;
  var expiresAt = authResp.expires_at;
  setCookie('id_token', idToken, expiresAt);
  location.reload();
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
    delete_cookie('id_token');
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

$(document).ready(function() {
});