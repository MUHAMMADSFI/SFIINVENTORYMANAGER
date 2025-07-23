document.getElementById('uploadForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    const formData = new FormData(this);

    const response = await fetch('/api/upload', {
        method: 'POST',
        body: formData
    });

    if (response.ok) {
        const data = await response.json();
        document.getElementById('result').textContent = JSON.stringify(data, null, 2);
    } else {
        document.getElementById('result').textContent = "Failed to upload or parse files.";
    }
});

async function processData() {
    const bomFile = document.getElementById('bomFile').files[0];
    const descriptionFile = document.getElementById('descriptionFile').files[0];
    const inventoryFile = document.getElementById('inventoryFile').files[0];
    const requirementsFile = document.getElementById('requirementsFile').files[0];
    const pipelineFile = document.getElementById('pipelineFile').files[0];
    const purchaseFile = document.getElementById("purchaseFile").files[0];
    if (!bomFile || !descriptionFile || !inventoryFile || !requirementsFile || !pipelineFile || !purchaseFile) {
        alert("Please select all five files.");
        return;
    }

    const formData = new FormData();
    formData.append("bomFile", bomFile);
    formData.append("descriptionFile", descriptionFile);
    formData.append("inventoryFile", inventoryFile);
    formData.append("purchaseFile", purchaseFile);
    formData.append("requirementsFile", requirementsFile);
    formData.append("pipelineFile", pipelineFile);

    try {
        const response = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            alert("Error: " + errorText);
            return;
        }

        const resultText = await response.text();
        document.getElementById('result').innerText = resultText;
    } catch (error) {
        alert("Error: " + error.message);
    }
}
// function to download all files
function downloadAll() {
    fetch('/api/download-all')
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.blob();
      })
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'productbuilder-data.zip';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
      })
      .catch(error => console.error('Error downloading files:', error));
  }