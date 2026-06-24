const formCreate = document.getElementById('account-create-form');
let usernameError = document.getElementById('username-error');
let emailError = document.getElementById('email-error');
let passwordError = document.getElementById('password-error');
let confirmPasswordError = document.getElementById('confirmPassword-error');

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