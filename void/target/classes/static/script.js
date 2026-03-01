
async function converter(GoalFormat) {
    let fileInput = document.getElementById('fileInput')
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
            const uuid = await res.text()
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
    const donwloadButton = document.getElementById('btn-download');
    const res = await fetch(`/application/status/${uuid}`);
    const data = await res.json();
    console.log("O JS leu isso aqui:", JSON.stringify(data));

    if (data.status === "FINALIZADO!"){
        donwloadButton.href = data.downloadUrl;
             
        alert("Conversão concluída com sucesso!");
        
    }
    else if (data.status !== 'FINALIZADO!'){
        setTimeout(() => conversionStatus(uuid), 2000);
    }
}