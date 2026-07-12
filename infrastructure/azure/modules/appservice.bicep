param location string
param namePrefix string
param entraTenantId string
param entraApiClientId string
param entraSpaClientId string
param sqlServerName string
param sqlDatabaseName string
param keyVaultUri string
param appInsightsConnectionString string
param appSubnetId string

var planName = '${namePrefix}-plan'
var apiAppName = take(replace('${namePrefix}-api', '-', ''), 60)
var webAppName = take(replace('${namePrefix}-web', '-', ''), 60)

resource plan 'Microsoft.Web/serverfarms@2023-01-01' = {
  name: planName
  location: location
  sku: {
    name: 'P1v3'
    tier: 'PremiumV3'
    size: 'P1v3'
    capacity: 1
  }
  kind: 'linux'
  properties: {
    reserved: true
  }
}

resource apiApp 'Microsoft.Web/sites@2023-01-01' = {
  name: apiAppName
  location: location
  kind: 'app,linux'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: plan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'JAVA|21-java21'
      alwaysOn: true
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      healthCheckPath: '/api/actuator/health'
      appSettings: [
        { name: 'SPRING_PROFILES_ACTIVE', value: 'azure' }
        { name: 'PORT', value: '8080' }
        { name: 'AZURE_KEYVAULT_ENDPOINT', value: keyVaultUri }
        { name: 'AZURE_SQL_SERVER', value: sqlServerName }
        { name: 'AZURE_SQL_DATABASE', value: sqlDatabaseName }
        { name: 'AZURE_SQL_USERNAME', value: '@Microsoft.KeyVault(SecretUri=${keyVaultUri}secrets/sql-admin-username/)' }
        { name: 'AZURE_SQL_PASSWORD', value: '@Microsoft.KeyVault(SecretUri=${keyVaultUri}secrets/sql-admin-password/)' }
        { name: 'AZURE_ENTRA_TENANT_ID', value: entraTenantId }
        { name: 'AZURE_ENTRA_API_CLIENT_ID', value: entraApiClientId }
        { name: 'APP_DATA_INITIALIZE', value: 'true' }
        { name: 'APP_CORS_ALLOWED_ORIGINS', value: 'https://${webAppName}.azurewebsites.net,https://*.azurefd.net' }
        { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appInsightsConnectionString }
        { name: 'ApplicationInsightsAgent_EXTENSION_VERSION', value: '~3' }
        { name: 'WEBSITES_PORT', value: '8080' }
      ]
    }
    virtualNetworkSubnetId: appSubnetId
  }
}

resource webApp 'Microsoft.Web/sites@2023-01-01' = {
  name: webAppName
  location: location
  kind: 'app,linux'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: plan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'NODE|22-lts'
      alwaysOn: true
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      appSettings: [
        { name: 'VITE_AUTH_MODE', value: 'entra' }
        { name: 'VITE_API_BASE_URL', value: 'https://${apiAppName}.azurewebsites.net/api' }
        { name: 'VITE_ENTRA_TENANT_ID', value: entraTenantId }
        { name: 'VITE_ENTRA_CLIENT_ID', value: entraSpaClientId }
        { name: 'VITE_ENTRA_API_SCOPE', value: 'api://${entraApiClientId}/access_as_user' }
        { name: 'SCM_DO_BUILD_DURING_DEPLOYMENT', value: 'true' }
      ]
    }
    virtualNetworkSubnetId: appSubnetId
  }
}

output apiAppName string = apiApp.name
output webAppName string = webApp.name
output apiAppHostname string = apiApp.properties.defaultHostName
output webAppHostname string = webApp.properties.defaultHostName
output apiPrincipalId string = apiApp.identity.principalId
output webPrincipalId string = webApp.identity.principalId
