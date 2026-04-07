let formatList = document.getElementById('format-list');
let fileInput = document.getElementById('file-Input');
let downloadButton = document.getElementById('btn-download');
let labelInput = document.getElementById('label-Input');
let main = document.querySelector('main');
let body = document.querySelector('body');
let overlay =  document.getElementById('drag-overlay');
let loadingText = document.getElementById('loading-text');
let divLoading = document.getElementById('div-loading');
let loadingContainer = document.getElementById('loading-container');
let filename = document.getElementById('file-name');
let canOverlay = true;

const formatMap = {
    video: ['mp4', 'mov', 'avi', 'mkv', 'webm', 'wmv'],
    audio: ['mp3', 'wav', 'ogg', 'flac'],
    image: ['png', 'jpeg', 'webp', 'gif', 'avif'],
    document: ['pdf', 'txt']
}

async function converter(GoalFormat) {
    canOverlay = false;
    downloadButton.style.display = "none";
    downloadButton.href = "#";
    
    let file = fileInput.files[0]
    let formData = new FormData();
    formData.append('file', file);
    formData.append('format', GoalFormat);
    try{
        let res = await fetch('/convert', {
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
    const res = await fetch(`/status/${uuid}`);
    const data = await res.json();
    console.log("O JS leu isso aqui:", JSON.stringify(data));

    if (data.status === "FINALIZADO!"){
        console.trace("O botão está sendo mostrado agora por causa deste UUID:", uuid);
        hideSpinner();
        downloadButton.style.display = "block";
        downloadButton.href = data.downloadUrl;
        
        labelInput.style.cursor = "pointer";
        fileInput.disabled = false;
        fileInput.value = "";

        labelInput.innerHTML = `<strong>Clique para selecionar</strong> ou arraste o vídeo aqui`;
        
        canOverlay = true;

        alert("Conversão concluída com sucesso!");
        
    }
    else if (data.status !== 'FINALIZADO!'){
        
        labelInput.style.cursor = "wait";
        fileInput.disabled = true;
        showSpinner(`${data.status}`);
        
        
        divLoading.style.display = "block";

       statusTimeOut = setTimeout(() => conversionStatus(uuid), 2000);


    }
}

fileInput.addEventListener("change", () => {
    formatList.innerHTML = '';
    console.log('O código chegou aqui!')
    const file = fileInput.files[0];
    const fileType = file.type;
    const category = fileType.split('/')[0];
    const fileFormat = fileType.split('/')[1];
    console.log("O arquivo foi selecionado:", file);
    console.log(fileType);
    if (fileInput.files.length> 0) {
        let temporaryFileName = file.name;
        let extension = temporaryFileName.substring(temporaryFileName.lastIndexOf('.') + 1);
        if (temporaryFileName.length > 40) {
            temporaryFileName = temporaryFileName.substring(0, 32) + "... " + extension;
        }
        console.log('Arquivo existe')
        filename.style.display = 'block';
        filename.innerText = temporaryFileName;
    }
    downloadButton.style.display = "none";
    downloadButton.href = "#";
    formatList.style.display = "block";    
    generateFormatList(category, fileFormat);
});

function generateFormatList(category, fileFormat){
    const formatCategory = formatMap[category] || [];
    formatCategory.forEach(element => {
        if (element === fileFormat) return;
        const newListItem = document.createElement('li');
        const newButton = document.createElement('button');
        newListItem.classList.add('format-item');
        newButton.innerText = `Converter para .${element}`
        newButton.addEventListener("click", () =>{
            converter(element);
    });
        
        newListItem.appendChild(newButton);
        formatList.appendChild(newListItem);
    });
};

//Drag n Drop
let dragCounter = 0; 

body.addEventListener('dragenter', (e) => {
    if (!canOverlay) return;
    e.preventDefault();
    dragCounter++;
    overlay.classList.add('active');
});

body.addEventListener('dragleave', (e) => {
    if (!canOverlay) return;
    e.preventDefault();
    dragCounter--;
    if (dragCounter === 0) {
        overlay.classList.remove('active');
    }
});

body.addEventListener('dragover', (e) => {
    if (!canOverlay) return;
    e.preventDefault(); 
});

body.addEventListener('drop', (e) => {
    if (!canOverlay) return;
    formatList.innerHTML = '';
    e.preventDefault();
    
    dragCounter = 0;
    overlay.classList.remove('active');
    
    const files = e.dataTransfer.files;
    downloadButton.style.display = "none";
    downloadButton.href = "#";
    if (files.length > 0) {
        fileInput.files = files;
        const file = fileInput.files[0];
        const fileType = file.type;
        const category = fileType.split('/')[0];
        const fileFormat = fileType.split('/')[1];    
        formatList.style.display = "block";
        
        if (fileInput.files.length> 0) {
        
            let temporaryFileName = file.name;
            let extension = temporaryFileName.substring(temporaryFileName.lastIndexOf('.') + 1);
            if (temporaryFileName.length > 40) {
                temporaryFileName = temporaryFileName.substring(0, 32) + "... " + extension;
            }
            console.log('Arquivo existe')
            filename.style.display = 'block';
            filename.innerText = temporaryFileName;
        }
        downloadButton.style.display = "none";
        downloadButton.href = "#";
        formatList.style.display = "block";    
        generateFormatList(category, fileFormat);

    }
     
});

function hideSpinner(){
    loadingContainer.style.display = "none";
    main.style.height = "auto";

}
function showSpinner(message){
    loadingContainer.style.display = "flex";
    loadingText.style.display = "block";
    loadingText.innerHTML = `<strong>${message} <strong>...</strong></strong>`;
    main.style.height = "150px";
}