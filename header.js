/**
 * Deals with the header buttons 
 */

document.addEventListener('DOMContentLoaded', updateHeader());

function getID() {
    const cookie = document.cookie; // returns "username=id; exp..."
	
	// search for the content of username
    const prefix = 'username=';
	// starting position of 'username='
    const start = cookie.indexOf(prefix);
    if (start === -1) return null;
	// starting positiong of ';' after 'username=' 
    let end = cookie.indexOf(';', start);
    if (end === -1) end = cookie.length;
	// create return value with the actual username
    const value = cookie.substring(start + prefix.length, end).trim();
	
	
    return decodeURIComponent(value); // account for encoded special character
}

function updateHeader() {
	const userCookie = getID(); // check if username field is valid
    const currentPath = window.location.pathname; // deal with selected button

    const headerElement = document.getElementById('header'); 
    if (!headerElement) return;

	// Set header content dynamically
	if (!userCookie) {
	    headerElement.innerHTML = `
	        <div class="site-title">Artist Search</div>
	        <div class="header-buttons">
	            <button onclick="location.href='index.html'" id="search_btn">Search</button>
	            <button onclick="location.href='login.html'" id="login_btn">Log in</button>
	            <button onclick="location.href='register.html'" id="signup_btn">Register</button>
	        </div>`;
		// deal with selected button based on current page
		if (currentPath.includes('index.html') ) {
            document.getElementById('search_btn').classList.add('selected');
        } else if (currentPath.includes('login.html')) {
            document.getElementById('login_btn').classList.add('selected');
        } else if (currentPath.includes('register.html')) {
            document.getElementById('signup_btn').classList.add('selected');
        }
	} else {
		headerElement.innerHTML = `
	        <div class="site-title">Artist Search</div>
	        <div class="header-buttons">
	            <button onclick="location.href='index.html'" id="search_btn">Search</button>
	            <button id="favorites_btn">Favorites</button>
	            <button onclick="logout()" id="logout_btn">Log out</button>
	        </div>`;
		// deal with selected button based on current page
		if (currentPath.includes('index.html') ) {
            document.getElementById('search_btn').classList.add('selected');
        } 
	}

}

function logout() {
	// clear cookie
    document.cookie = 'username=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
	// redirect window
    window.location.href = 'index.html';
}


