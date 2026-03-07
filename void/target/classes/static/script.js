let formatList = document.getElementById('format-list');
let fileInput = document.getElementById('file-Input');
let downloadButton = document.getElementById('btn-download');
let labelInput = document.getElementById('label-Input');


async function converter(GoalFormat) {
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
        
        downloadButton.style.display = "block";
        downloadButton.href = data.downloadUrl;
        
        labelInput.style.cursor = "pointer";
        fileInput.disabled = false;

        labelInput.innerHTML = `<strong>${data.status}</strong>`;
        alert("Conversão concluída com sucesso!");
        
    }
    else if (data.status !== 'FINALIZADO!'){
        
        labelInput.style.cursor = "wait";
        fileInput.disabled = true;
        labelInput.innerHTML = `<strong>${data.status} <strong>...</strong></strong>`;
        
        setTimeout(() => conversionStatus(uuid), 2000);


    }
}
fileInput.addEventListener("change", () => {
    formatList.style.display = "block";
});


