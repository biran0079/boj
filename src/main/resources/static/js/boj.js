
function setCookie(sName,sValue) {
    var oDate = new Date();
    oDate.setYear(oDate.getFullYear() + 1);
    var sCookie = encodeURIComponent(sName) + '=' + encodeURIComponent(sValue) + ';expires=' + oDate.toGMTString() + ';path=/';
    document.cookie = sCookie;
}
function delete_cookie(name) {
  document.cookie = name +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}
var onSignIn = function (googleUser) {
  console.log("signed in");
  var id_token = googleUser.getAuthResponse().id_token;
  setCookie('id_token', id_token);
  location.reload();
}

function onLoad() {
    gapi.signin2.render('my-signin2', {
        'scope': 'profile email',
        'width': 208,
        'height': 45,
        'longtitle': true,
        'theme': 'dark',
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
