/* skeleton code from CSCI 201 JavaScript and AJAX Lectures */
function validateEmailLogin() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "Validate", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	const input = document.myform.emaillogin.value;
	xhttp.onload = function() {
		document.getElementById("emailLoginError").innerHTML = this.responseText;
		
		loginValidator.emaillogin = (this.responseText === "");
		updateLoginButton();
		
		//if there's been no validation issue, remove the icon class
		//help from https://stackoverflow.com/a/507157
		if(this.responseText === "")
		{
			document.getElementById("emaillogin").classList.remove("icon");
		}
		else { //else there's been a validation issue, add the icon class
			document.getElementById("emaillogin").classList.add("icon");
		}
		
	}
	
	xhttp.send("field=emaillogin&emaillogin=" + input);
}

function validateEmail() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "Validate", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	xhttp.onload = function() {
		document.getElementById("emailError").innerHTML = this.responseText;
		registerValidator.email = (this.responseText === "");
		updateRegisterButton();
		
		//if there's been no validation issue, remove the icon class
		//help from https://stackoverflow.com/a/507157
		if(this.responseText === "")
		{
			document.getElementById("email").classList.remove("icon");
		}
		else { //else there's been a validation issue, add the icon class
			document.getElementById("email").classList.add("icon");
		}
	}
	
	xhttp.send("field=email&email=" + document.myform.email.value);
}
	
function validatePassword() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", "Validate?field=password&password=" + document.myform.password.value, true);
	
	xhttp.onload = function() {
		document.getElementById("passwordError").innerHTML = this.responseText;
		registerValidator.password = (this.responseText === "");
		updateRegisterButton();
		
		loginValidator.password = (this.responseText === "");
		updateLoginButton();
		
		//if there's been no validation issue, remove the icon class
		//help from https://stackoverflow.com/a/507157
		if(this.responseText === "")
		{
			document.getElementById("password").classList.remove("icon");
		}
		else { //else there's been a validation issue, add the icon class
			document.getElementById("password").classList.add("icon");
		}
	}
	
	xhttp.send();
}

function validateName() {
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", "Validate?field=fullname&fullname=" + document.myform.fullname.value, true);
	
	xhttp.onload = function() {
		document.getElementById("fullnameError").innerHTML = this.responseText;
		registerValidator.fullname = (this.responseText === "");
		updateRegisterButton();
		
		//if there's been no validation issue, remove the icon class
		//https://stackoverflow.com/a/507157
		if(this.responseText === "")
		{
			document.getElementById("fullname").classList.remove("icon");
		}
		else { //else there's been a validation issue, add the icon class
			document.getElementById("fullname").classList.add("icon");
		}
	}
	
	xhttp.send();
}

/*validator help from https://stackoverflow.com/a/69386726 */
let registerValidator = {
	fullname: false,
	email: false,
	password: false
};

function updateRegisterButton() {
	let allTrue = Object.values(registerValidator).every(Boolean);
	if (allTrue) {
      document.getElementById("button-send").disabled = false;
	  document.getElementById("button-send").classList.add("active"); //turns the color to the "active color"
	  //aid from https://stackoverflow.com/a/507157
    } else {
      document.getElementById("button-send").disabled = true;
	  document.getElementById("button-send").classList.remove("active");
    }
}

let loginValidator = {
	emaillogin: false,
	password: false
};

function updateLoginButton() {
	let allTrue = Object.values(loginValidator).every(Boolean);
	if (allTrue) {
      document.getElementById("button-send").disabled = false;
	  document.getElementById("button-send").classList.add("active"); //turns the color to the "active color"
	  //aid from https://stackoverflow.com/a/507157
    } else {
      document.getElementById("button-send").disabled = true;
	  document.getElementById("button-send").classList.remove("active");
    }
}


/*getCookie(name) idea from https://stackoverflow.com/a/15724300*/
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function updateHeader() {
    const header = document.getElementById("header-right");
    const loginemail = getCookie("loginemail");

	//if we're logged in, handle accordingly
    if (loginemail) {
		if(window.location.href.includes("search.html"))
		{
			header.innerHTML = `
	            <a href="search.html" class="blue-button">Search</a>
	            <a href="favorites.html">Favorites</a>
	            <a href="#" onclick="logout()">Log out</a>
	        `;	
		} else if(window.location.href.includes("favorites.html"))
			header.innerHTML = `
				<a href="search.html">Search</a>
		        <a href="favorites.html" class="blue-button">Favorites</a>
		        <a href="#" onclick="logout()">Log out</a>
        	`;
    } 
	//if we're logged out, handle accordingly
	else {
		if(window.location.href.includes("search.html"))
		{
			header.innerHTML = `
	            <a href="search.html" class="blue-button">Search</a>
	            <a href="login.html">Log in</a>
	            <a href="register.html">Register</a>
	        `;	
		} else if(window.location.href.includes("login.html")) {
			header.innerHTML = `
	            <a href="search.html">Search</a>
	            <a href="login.html" class="blue-button">Log in</a>
	            <a href="register.html">Register</a>
	        `;	
		} else if(window.location.href.includes("register.html")) {
			header.innerHTML = `
	            <a href="search.html">Search</a>
	            <a href="login.html">Log in</a>
	            <a href="register.html" class="blue-button">Register</a>
	        `;	
		}
        
    }
}

/*implementation from the Assignment 3 guidance pdf*/
function logout() {
	//setting the expiration into the past to kill the cookie
	document.cookie = "loginemail=; path=/; expires=Sat, 01 Jan 2000 00:00:00 UTC;";
    window.location.href = "search.html"; //default page should be the search page
}

window.onload = function () {
    updateHeader();
}

/* skeleton code from CSCI 201 JavaScript and AJAX Lectures */
function validateLogin() {
    var email = document.myform.emaillogin.value;
    var password = document.myform.password.value;
    var params = `emaillogin=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;

    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "LoginServlet", true);
    xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    xhttp.onload = function() {
        try {
            var response = JSON.parse(this.responseText);
            if (response.success) {
                if (response.accountType === "Student") {
                    window.location.href = "/finalproject/student";
                } else if (response.accountType === "TA") {
                    window.location.href = "/finalproject/ta";
                } else {
                    document.getElementById("passwordError").innerText = "Unknown account type.";
                }
            } else {
                document.getElementById("passwordError").innerText = response.message;
            }
        } catch (e) {
            document.getElementById("passwordError").innerText = "Error logging in.";
        }
    };

    xhttp.send(params);
    return false;
}

/* skeleton code from CSCI 201 JavaScript and AJAX Lectures */
function validateRegistration() {
	var fullname = document.myform.fullname.value;
	var email = document.myform.email.value;
	var password = document.myform.password.value;
	var accountTypeElement = document.querySelector('input[name="account_type"]:checked');
	var accountType = accountTypeElement ? accountTypeElement.value : "";

	if (!fullname || !email || !password || !accountType) {
		alert("Please fill in all fields and select account type.");
		return false;
	}

	var params = `fullname=${encodeURIComponent(fullname)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&account_type=${encodeURIComponent(accountType)}`;

	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "RegisterServlet", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

	xhttp.onload = function() {
		try {
			var response = JSON.parse(this.responseText);
			if (response.success) {
				if (response.accountType === "Student") {
					window.location.href = "/finalproject/student";
				} else if (response.accountType === "TA") {
					window.location.href = "/finalproject/ta";
				} else {
					alert("Unknown account type. Please log in manually.");
				}
			} else {
				document.getElementById("emailError").innerText = response.message;
			}
		} catch (e) {
			document.getElementById("emailError").innerText = "Error during registration.";
		}
	};

	xhttp.send(params);
	return false;
}