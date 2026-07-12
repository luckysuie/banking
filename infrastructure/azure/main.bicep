@description('Azure region for all resources')
param location string = resourceGroup().location

@description('Short name prefix for resources (e.g. cloudbank)')
param namePrefix string = 'cloudbank'

@description('Microsoft Entra ID admin email for Azure SQL Database access')
param entraAdminEmail string

@description('Entra tenant ID')
param entraTenantId string

@description('Entra API app registration client ID')
param entraApiClientId string

@description('Entra SPA app registration client ID')
param entraSpaClientId string

@description('Custom DNS zone name (e.g. cloudbank.example.com). Leave empty to skip DNS.')
param dnsZoneName string = ''

@description('Deploy optional Linux and Windows VMs for Site Recovery')
param deployVirtualMachines bool = true

var uniqueSuffix = uniqueString(resourceGroup().id)
var resourcePrefix = '${namePrefix}-${uniqueSuffix}'

module networking 'modules/networking.bicep' = {
  name: 'networking'
  params: {
    location: location
    namePrefix: resourcePrefix
  }
}

module storage 'modules/storage.bicep' = {
  name: 'storage'
  params: {
    location: location
    namePrefix: resourcePrefix
  }
}

module keyVault 'modules/keyvault.bicep' = {
  name: 'keyvault'
  params: {
    location: location
    namePrefix: resourcePrefix
    entraTenantId: entraTenantId
    subnetId: networking.outputs.keyVaultSubnetId
  }
}

module sql 'modules/sql.bicep' = {
  name: 'sql'
  params: {
    location: location
    namePrefix: resourcePrefix
    entraAdminEmail: entraAdminEmail
    keyVaultName: keyVault.outputs.keyVaultName
    subnetId: networking.outputs.dataSubnetId
  }
}

module monitoring 'modules/monitoring.bicep' = {
  name: 'monitoring'
  params: {
    location: location
    namePrefix: resourcePrefix
  }
}

module appservice 'modules/appservice.bicep' = {
  name: 'appservice'
  params: {
    location: location
    namePrefix: resourcePrefix
    entraTenantId: entraTenantId
    entraApiClientId: entraApiClientId
    entraSpaClientId: entraSpaClientId
    sqlServerName: sql.outputs.sqlServerName
    sqlDatabaseName: sql.outputs.sqlDatabaseName
    keyVaultUri: keyVault.outputs.keyVaultUri
    appInsightsConnectionString: monitoring.outputs.appInsightsConnectionString
    appSubnetId: networking.outputs.appSubnetId
  }
}

module keyVaultAccess 'modules/keyvault-access.bicep' = {
  name: 'keyvault-access'
  params: {
    keyVaultName: keyVault.outputs.keyVaultName
    apiPrincipalId: appservice.outputs.apiPrincipalId
    webPrincipalId: appservice.outputs.webPrincipalId
  }
}

module appGateway 'modules/application-gateway.bicep' = {
  name: 'appgateway'
  params: {
    location: location
    namePrefix: resourcePrefix
    gatewaySubnetId: networking.outputs.gatewaySubnetId
    apiAppHostname: appservice.outputs.apiAppHostname
    webAppHostname: appservice.outputs.webAppHostname
  }
}

module frontDoor 'modules/frontdoor.bicep' = {
  name: 'frontdoor'
  params: {
    namePrefix: resourcePrefix
    apiOriginHostname: appservice.outputs.apiAppHostname
    webOriginHostname: appservice.outputs.webAppHostname
    customDomain: dnsZoneName
  }
}

module dns 'modules/dns.bicep' = if (!empty(dnsZoneName)) {
  name: 'dns'
  params: {
    dnsZoneName: dnsZoneName
    frontDoorEndpointHostname: frontDoor.outputs.endpointHostname
  }
}

module firewall 'modules/firewall.bicep' = {
  name: 'firewall'
  params: {
    location: location
    namePrefix: resourcePrefix
    vnetName: networking.outputs.vnetName
    firewallSubnetId: networking.outputs.firewallSubnetId
  }
}

module bastion 'modules/bastion.bicep' = {
  name: 'bastion'
  params: {
    location: location
    namePrefix: resourcePrefix
    bastionSubnetId: networking.outputs.bastionSubnetId
  }
}

module virtualMachines 'modules/virtual-machines.bicep' = if (deployVirtualMachines) {
  name: 'virtual-machines'
  params: {
    location: location
    namePrefix: resourcePrefix
    vmSubnetId: networking.outputs.vmSubnetId
  }
}

module siteRecovery 'modules/site-recovery.bicep' = if (deployVirtualMachines) {
  name: 'site-recovery'
  params: {
    location: location
    namePrefix: resourcePrefix
    linuxVmId: virtualMachines.outputs.linuxVmId
    windowsVmId: virtualMachines.outputs.windowsVmId
  }
}

output resourceGroupName string = resourceGroup().name
output apiUrl string = 'https://${appservice.outputs.apiAppHostname}/api'
output webUrl string = 'https://${appservice.outputs.webAppHostname}'
output frontDoorUrl string = frontDoor.outputs.endpointUrl
output keyVaultUri string = keyVault.outputs.keyVaultUri
output sqlServerFqdn string = '${sql.outputs.sqlServerName}.database.windows.net'
output appInsightsConnectionString string = monitoring.outputs.appInsightsConnectionString
output entraSqlAdminScript string = sql.outputs.entraAdminSqlScript
