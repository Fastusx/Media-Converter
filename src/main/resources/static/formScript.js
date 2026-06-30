const formCreate = document.getElementById('account-create-form');
const formLogin = document.getElementById('login-form');
let usernameError = document.getElementById('username-error');
let emailError = document.getElementById('email-error');
let passwordError = document.getElementById('password-error');
let confirmPasswordError = document.getElementById('confirmPassword-error');
let usernameErrorLogin = document.getElementById('username-error-login');
let passwordErrorLogin = document.getElementById('password-error-login');
let loginError = document.getElementById('login-error');
let guestOptions = document.getElementById('guest-options');
let userOptions = document.getElementById('user-options');
let showUsername = document.getElementById('show-username');

let username = localStorage.getItem('username');

document.addEventListener('DOMContentLoaded', () => {
    if (guestOptions && userOptions && showUsername){
    if (!localStorage.getItem('token')){
        guestOptions.style.display = 'flex';
        userOptions.style.display = 'none';
        showUsername.textContent = '';        
    }else{
        guestOptions.style.display = 'none';
        userOptions.style.display = 'flex';
        showUsername.textContent = username;
    }
}
});

function logout(){
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    window.location.href = '/';
}
if (formCreate){
formCreate.addEventListener('submit', async (event) => {
    event.preventDefault();
    usernameError.style.display = 'none';
    emailError.style.display = 'none';
    passwordError.style.display = 'none';
    confirmPasswordError.style.display = 'none';
    
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    let res = await fetch('/api/createAccount', {
        method: 'POST',
        headers:{'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    

    if (res.ok){
        alert('Conta criada com sucesso!');
        window.location.href = '/';
        return;
    }
    
    const StatusMessage = await res.text();
    errorMessage(StatusMessage);
    
});
}

if (formLogin){
formLogin.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    if (!data.username || !data.password){
        if (!data.username){
        errorLabels(usernameErrorLogin, "Esse campo é obrigatório.");
        }
        if (!data.password){
        errorLabels(passwordErrorLogin, "Esse campo é obrigatório.");
        }
        return;
    }

    let res = await fetch('/api/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    if (res.ok){
       const responseData = await res.json();
        localStorage.setItem('token', responseData.token);
        localStorage.setItem('username', responseData.username);
        window.location.href = '/';
       } 
    else{
        const statusMessage = await res.text();
        errorLabels(loginError, statusMessage);
       }
    })
}

function errorMessage(message){
    if (message.includes("nome")) {
        errorLabels(usernameError, message);
    } 

    if (message.includes("E-mail")) {
        errorLabels(emailError, message);
    }
    
    if (message.includes("senha")) {
        errorLabels(passwordError, message);
    }
    
    if(message.includes("coincidem")){
        errorLabels(passwordError, message);
        errorLabels(confirmPasswordError, message);
    }
}

function errorLabels(label, message){
    label.textContent = message
    label.style.display = 'block';
    label.style.color = 'red';
}

