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
		if(window.location.href.includes("index.html"))
		{
			header.innerHTML = `
	            <a href="index.html" class="blue-button">Search</a>
	            <a href="favorites.html">Favorites</a>
	            <a href="#" onclick="logout()">Log out</a>
	        `;	
		} else if(window.location.href.includes("favorites.html"))
			header.innerHTML = `
				<a href="index.html">Search</a>
		        <a href="favorites.html" class="blue-button">Favorites</a>
		        <a href="#" onclick="logout()">Log out</a>
        	`;
    } 
	//if we're logged out, handle accordingly
	else {
		if(window.location.href.includes("index.html"))
		{
			header.innerHTML = `
	            <a href="index.html" class="blue-button">Search</a>
	            <a href="login.html">Log in</a>
	            <a href="register.html">Register</a>
	        `;	
		} else if(window.location.href.includes("login.html")) {
			header.innerHTML = `
	            <a href="index.html">Search</a>
	            <a href="login.html" class="blue-button">Log in</a>
	            <a href="register.html">Register</a>
	        `;	
		} else if(window.location.href.includes("register.html")) {
			header.innerHTML = `
	            <a href="index.html">Search</a>
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
    window.location.href = "index.html"; //default page should be the search page
}

window.onload = function () {
    updateHeader();
}

/* skeleton code from CSCI 201 JavaScript and AJAX Lectures */
function validateLogin() {
	//figured out the parameters to send thanks to https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent
	var email = document.myform.emaillogin.value;
	var password = document.myform.password.value;
	var params = `emaillogin=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;

	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "LoginServlet", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	xhttp.onload = function() {
		document.getElementById("passwordError").innerText = this.responseText;	
		if(this.responseText === "")
		{
			window.location.replace("index.html");
		}
	}
	
	xhttp.send(params);
	return false;
}

/* skeleton code from CSCI 201 JavaScript and AJAX Lectures */
function validateRegistration() {
	//figured out the parameters to send thanks to https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent
	var fullname = document.myform.fullname.value;
	var email = document.myform.email.value;
	var password = document.myform.password.value;
	var params = `fullname=${encodeURIComponent(fullname)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;

	
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "RegisterServlet", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	xhttp.onload = function() {
		document.getElementById("emailError").innerText = this.responseText;
		if(this.responseText === "")
		{
			window.location.replace("index.html");
		}			
	}
	
	xhttp.send(params);
	return false;
}

/*retrieves the token from Artsy API */
function authenticate(callback) {

	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "ArtsyTokenServlet", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	xhttp.onload = function() {
		//gets a new token before each request
		//localStorage help from https://stackoverflow.com/questions/16206322/how-to-get-js-variable-to-retain-value-after-page-refresh
		localStorage.setItem("token", this.responseText);
		//document.getElementById("searchresult1").innerHTML = localStorage.getItem("token");
		if(callback) callback(this.responseText); //ensures that the api is only called after I get a token
	}
	
	xhttp.send();
}

const artists = new Map();

function searchArtists() {
	
	//clear the document of any search results that were there before
	document.getElementById("artistinfo").innerHTML = ""; //clears out previous search results
	document.getElementById("searchresults").innerHTML = ""; //clears out previous search results
	document.getElementById("noresults").innerHTML = ""; //places the no results found popup
	document.getElementById("loading").innerHTML = "<img src=\"images/loading.gif\" class=\"loading\">"; //loading gif
	
	var query = document.myform.search.value;
	
	authenticate(function(token) {
		var xhttp = new XMLHttpRequest();
	    xhttp.open("GET", "https://api.artsy.net/api/search?q=" + encodeURIComponent(query) + "&size=10&type=artist");
	    xhttp.setRequestHeader("X-Xapp-Token", token);
	    xhttp.onload = function () {
	        const jsonObject = JSON.parse(this.responseText);
			
			//for debugging
			//var jsonString = JSON.stringify(jsonObject);
			//document.getElementById("searchresult2").innerHTML = jsonString;
			
			//get the results array from the json object
			//square bracket notation from assignment 3 instructions
			const results = jsonObject["_embedded"]["results"];
			//document.getElementById("searchresult3").innerHTML = "Length of results array: " + results.length;
			
			var HTMLstring = "";
			//iterating over a javascript array: learned from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/length
			for(let i = 0; i < results.length; i++)
			{
				var name = results[i]["title"];
				var thumbnail = results[i]["_links"]["thumbnail"]["href"];
				var artistID = results[i]["_links"]["self"]["href"].substring(34);
				artists.set(name, artistID);
				
				//if missing png, do this instead
				if(thumbnail == "/assets/shared/missing_image.png")
				{
					thumbnail = "images/artsy_logo.svg";
				}
				
				HTMLstring += "<div class=\"card\" onclick=\"getArtistInfo(this);\"><img src=" + thumbnail + "><div class=\"container\">";
				HTMLstring += "<p>" + name + "</p></div></div>";
			}
			
			document.getElementById("loading").innerHTML = ""; //clearing the loading gif before showing results
			//if there are no results, put a button that says no results were found.
			if(results.length === 0)
			{
				HTMLstring = "<div class=\"noresults\"><p>No results found.</p></div>";
				document.getElementById("searchresults").innerHTML = ""; //clears out previous search results
				document.getElementById("noresults").innerHTML = HTMLstring; //places the no results found popup
			}
			else{
				document.getElementById("searchresults").innerHTML = HTMLstring;	
				document.getElementById("noresults").innerHTML = ""; //gets rid of the no results found popup
			}
	    }; 
	    xhttp.send();
	});
    return false;
}

function getArtistInfo(div) {
	
	//turn all cards back to normal
	//help from https://stackoverflow.com/a/16985925
	var cards = document.getElementsByClassName("card");
	for(let i =0; i < cards.length; i++)
	{
		var card = cards[i];
		card.classList.remove("card-clicked");
	}
	
	//clicked card turns dark
	//help adding classes to an element from https://stackoverflow.com/a/507157
	div.classList.add("card-clicked");
	
	var useremail = getCookie("loginemail");
	
	//clear the document of previous searches
	document.getElementById("artistinfo").innerHTML = ""; //clears out previous search results
	document.getElementById("loading2").innerHTML = "<img src=\"images/loading.gif\">";
	
	//for debugging purposes
	//document.getElementById("clicked").innerHTML = div.innerText;
	var key = div.innerText;
	var artistID = artists.get(key);
	
	authenticate(function(token) {
		var xhttp = new XMLHttpRequest();
	    xhttp.open("GET", "https://api.artsy.net/api/artists/" + artistID);
	    xhttp.setRequestHeader("X-Xapp-Token", token);
	    xhttp.onload = function () {
	        const jsonObject = JSON.parse(this.responseText);
			
			//for debugging
			//var jsonString = JSON.stringify(jsonObject);
			//document.getElementById("getartist1").innerHTML = jsonString;
			
			//parse the jsonObject for parameters
			var name = jsonObject["name"];
			var birthday = jsonObject["birthday"];
			var deathday = jsonObject["deathday"];
			var nationality = jsonObject["nationality"];
			var biography = jsonObject["biography"];
			
			
			var HTMLstring = "<div class=\"bio\"><p class=\"biotitle\">" + name + " (" + birthday + " - " + deathday + ") ";
				/*+ "<span id=\"" + artistID + "\" onclick=\"doFavorite(this);\">"
				+ "<button type=\"button\" id=\"starbutton\" class=\"starbutton\">☆</button></span>"
				+ "</p><p class=\"subtitle\">" + nationality + "</p>"
				+ "<p class=\"fulltext\">" + biography + "</p></div>";*/
			
			//HTMLstring += "<span id=\"" + artistID + "\" onclick=\"doFavorite(this);\">"
			//				+ "<button type=\"button\" id=\"starbutton\" class=\"starbutton\">☆</button></span>";
			
			if(useremail) //if the user is logged in, they can access the favorites button
			{
				HTMLstring += "<span id=\"" + artistID + "\" onclick=\"doFavorite(this);\">"
				+ "<button type=\"button\" id=\"starbutton\" class=\"starbutton\">☆</button></span>";
			}
						
			HTMLstring	+= "</p><p class=\"subtitle\">" + nationality + "</p>"
				+ "<p class=\"fulltext\">" + biography + "</p></div>";
		
			//clearing the loading bar
			document.getElementById("loading2").innerHTML = "";
			document.getElementById("artistinfo").innerHTML = HTMLstring;
	    }; 
	    xhttp.send();
	});
}

function doFavorite(span) {
	var innerText = document.getElementById("starbutton").innerText;
	var artistID = span.id;
	
	if(innerText == "☆") //if this item is NOT a favorite, make it a favorite
	{
		document.getElementById("starbutton").innerHTML = "<span style=\"color: #fcc300\">★</span>";	
		insertFavorite(artistID);
	} 
	else { //if this item IS a favorite, unfavorite it
		document.getElementById("starbutton").innerHTML = "☆";	
		removeFavorite(artistID);
		//remove from favorites
	}
}

//these two functions track user favorites in the database
function insertFavorite(aid) {

	var useremail = getCookie("loginemail");
	var artistID = aid;
	var params = `email=${encodeURIComponent(useremail)}&artistid=${encodeURIComponent(artistID)}`;
	
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "InsertFavorite", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	xhttp.onload = function() {
		document.getElementById("insert").innerText = this.responseText;		
	}
	
	xhttp.send(params);
}

function removeFavorite(aid) {
	var useremail = getCookie("loginemail");
	var artistID = aid;
	var params = `email=${encodeURIComponent(useremail)}&artistid=${encodeURIComponent(artistID)}`;
	
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "RemoveFavorite", true);
	xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	xhttp.onload = function() {
		document.getElementById("remove").innerText = this.responseText;		
	}
	
	xhttp.send(params);
}

if(window.location.href.includes("favorites.html"))
{
	updateFavorites();
}

function updateFavorites() {
	const email = getCookie("loginemail");
	if (!email) {
		document.getElementById("favoriteresult").innerHTML = "<p>User not logged in.</p>";
		return;
	}

	//get artist information here, code copied over from my authenticate function
	authenticate(function(token) {
		var xhttp = new XMLHttpRequest();
		xhttp.open("POST", "GetFavorites", true);
		xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

		xhttp.onload = function() {
			//json object that's storing all the user's favorite artist IDs
			const artistIDs = JSON.parse(this.responseText);
			
			//there should be nothing on the page if there are no favorited artists
			if (artistIDs.length === 0) {
				document.getElementById("noresults").innerHTML = "<div class=\"noresults2\"><p>No favorite artists.</p></div>";
				document.getElementById("favoriteresult").innerHTML = "";
				return;
			} else {
				document.getElementById("noresults").innerHTML = "";
			}

			// Loop through favorites and get each artist
			var HTMLstring = "";

			for(let i=0; i < artistIDs.length; i++)
			{
				var id = artistIDs[i];
				var xhr = new XMLHttpRequest();
				xhr.open("GET", "https://api.artsy.net/api/artists/" + id, true);
				xhr.setRequestHeader("X-Xapp-Token", token);
				xhr.onload = function () {
					const jsonObject = JSON.parse(this.responseText);
					const name = jsonObject["name"];
					var thumbnail = jsonObject["_links"]["thumbnail"]["href"];
					
					//handles missing images
					if(thumbnail == "/assets/shared/missing_image.png")
					{
						thumbnail = "images/artsy_logo.svg";
					}
					
					artists.set(name, id);
					
					//code copied over from searchArtists();
					HTMLstring += "<div class=\"card\" onclick=\"getFavoriteArtistInfo(this);\"><img src=" + thumbnail + "><div class=\"container\">";
					HTMLstring += "<p>" + name + "</p></div></div>";

					if (i === (artistIDs.length - 1)) {
						document.getElementById("favoriteresult").innerHTML = HTMLstring;
					}
				};
				xhr.send();
			} 
		};

		xhttp.send("email=" + email);
	});
}

//code copied over from getArtistInfo(div)
function getFavoriteArtistInfo(div) {
	
	//clear the document of previous searches
	document.getElementById("artistinfo").innerHTML = ""; //clears out previous search results
	document.getElementById("loading2").innerHTML = "<img src=\"images/loading.gif\">";
	
	//turn all cards back to normal
	//help from https://stackoverflow.com/a/16985925
	var cards = document.getElementsByClassName("card");
	for(let i =0; i < cards.length; i++)
	{
		var card = cards[i];
		card.classList.remove("card-clicked");
	}
	
	//clicked card turns dark
	//help adding classes to an element from https://stackoverflow.com/a/507157
	div.classList.add("card-clicked");
	
	//for debugging purposes
	//document.getElementById("clicked").innerHTML = div.innerText;
	var key = div.innerText;
	var artistID = artists.get(key);
	
	authenticate(function(token) {
		var xhttp = new XMLHttpRequest();
	    xhttp.open("GET", "https://api.artsy.net/api/artists/" + artistID);
	    xhttp.setRequestHeader("X-Xapp-Token", token);
	    xhttp.onload = function () {
	        const jsonObject = JSON.parse(this.responseText);
			
			//for debugging
			//var jsonString = JSON.stringify(jsonObject);
			//document.getElementById("getartist1").innerHTML = jsonString;
			
			//parse the jsonObject for parameters
			var name = jsonObject["name"];
			var birthday = jsonObject["birthday"];
			var deathday = jsonObject["deathday"];
			var nationality = jsonObject["nationality"];
			var biography = jsonObject["biography"];
			
			
			var HTMLstring = "<div class=\"bio\"><p class=\"biotitle\">" + name + " (" + birthday + " - " + deathday + ") "
			
			HTMLstring += "<span id=\"" + artistID + "\" onclick=\"doFavorite(this);\">"
			+ "<button type=\"button\" id=\"starbutton\" class=\"starbutton\"><span style=\"color: #fcc300\">★</span></button></span>";
						
			HTMLstring	+= "</p><p class=\"subtitle\">" + nationality + "</p>"
				+ "<p class=\"fulltext\">" + biography + "</p></div>";
		
			//clearing the loading bar
			document.getElementById("loading2").innerHTML = "";
			document.getElementById("artistinfo").innerHTML = HTMLstring;
	    }; 
	    xhttp.send();
	});
}