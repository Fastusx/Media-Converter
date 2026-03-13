let formatList = document.getElementById('format-list');
let fileInput = document.getElementById('file-Input');
let downloadButton = document.getElementById('btn-download');
let labelInput = document.getElementById('label-Input');
let main = document.querySelector('main');
let overlay =  document.getElementById('drag-overlay');
let statusTimeOut = null;


async function converter(GoalFormat) {
    if (statusTimeOut){
        clearTimeout(statusTimeOut);
        statusTimeOut = null;
    }
    downloadButton.style.display = "none";
    downloadButton.href = "#";
    
    let file = fileInput.files[0]
    let formData = new FormData();
    formData.append('file', file);
    formData.append('format', GoalFormat);
    try{
        let res = await fetch('/application/convert', {
            method: 'POST',
            body: formData
        });
        if (res.ok) {
            fileInput.value = "";
            const uuid = await res.text()
            formatList.style.display = "none";
            conversionStatus(uuid);
        } else {
            alert("Erro ao converter o arquivo.");
        }
    } catch (error) {
        console.error("Erro durante a conversão:", error);
        alert("Ocorreu um erro durante a conversão.");
    }
}

async function conversionStatus(uuid) {
    const res = await fetch(`/application/status/${uuid}`);
    const data = await res.json();
    console.log("O JS leu isso aqui:", JSON.stringify(data));

    if (data.status === "FINALIZADO!"){
        console.trace("O botão está sendo mostrado agora por causa deste UUID:", uuid);
        downloadButton.style.display = "block";
        downloadButton.href = data.downloadUrl;
        
        labelInput.style.cursor = "pointer";
        fileInput.disabled = false;
        fileInput.value = "";

        labelInput.innerHTML = `<strong>${data.status}</strong>`;
        alert("Conversão concluída com sucesso!");
        
    }
    else if (data.status !== 'FINALIZADO!'){
        
        labelInput.style.cursor = "wait";
        fileInput.disabled = true;
        labelInput.innerHTML = `<strong>${data.status} <strong>...</strong></strong>`;
        
       statusTimeOut = setTimeout(() => conversionStatus(uuid), 2000);


    }
}

fileInput.addEventListener("change", () => {
    if (statusTimeOut){
        clearTimeout(statusTimeOut);
        statusTimeOut = null;
    }
    console.log("O arquivo foi selecionado:", fileInput.files[0]);
    downloadButton.style.display = "none";
    downloadButton.href = "#";
    formatList.style.display = "block";    
});

//Drag n Drop
let dragCounter = 0; 

main.addEventListener('dragenter', (e) => {
    e.preventDefault();
    dragCounter++;
    overlay.classList.add('active');
});

main.addEventListener('dragleave', (e) => {
    e.preventDefault();
    dragCounter--;
    if (dragCounter === 0) {
        overlay.classList.remove('active');
    }
});

main.addEventListener('dragover', (e) => {
    e.preventDefault(); 
});

main.addEventListener('drop', (e) => {
    e.preventDefault();
    dragCounter = 0;
    overlay.classList.remove('active');

    const files = e.dataTransfer.files;
    downloadButton.style.display = "none";
    downloadButton.href = "#";
    if (files.length > 0) {
        fileInput.files = files;
        formatList.style.display = "block";
    }
});