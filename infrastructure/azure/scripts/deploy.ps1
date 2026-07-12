param(
    [Parameter(Mandatory = $true)]
    [string]$ResourceGroup,

    [Parameter(Mandatory = $true)]
    [string]$Location = "canadacentral",

    [Parameter(Mandatory = $true)]
    [string]$ParametersFile = "main.parameters.json"
)

Write-Host "Creating resource group $ResourceGroup in $Location..."
az group create --name $ResourceGroup --location $Location

Write-Host "Deploying Azure infrastructure..."
az deployment group create `
    --resource-group $ResourceGroup `
    --template-file main.bicep `
    --parameters @$ParametersFile

Write-Host "Deployment complete. Retrieve outputs:"
az deployment group show `
    --resource-group $ResourceGroup `
    --name main `
    --query properties.outputs
