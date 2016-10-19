
document.setCookie = function(sName,sValue) {
    var oDate = new Date();
    oDate.setYear(oDate.getFullYear()+1);
    var sCookie = encodeURIComponent(sName) + '=' + encodeURIComponent(sValue) + ';expires=' + oDate.toGMTString() + ';path=/';
    document.cookie= sCookie;
}

function onSignIn(googleUser) {
  var id_token = googleUser.getAuthResponse().id_token;
  document.setCookie('id_token', id_token);
  window.location.href = '/problems';
}

function onLoad() {
    gapi.load('auth2', function() {
        gapi.auth2.init();
    });
    gapi.signin2.render('my-signin2', {
        'scope': 'profile email',
        'width': 200,
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
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
      window.location.href = '/';
    });
}
