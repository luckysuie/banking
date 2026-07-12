@description('Location')
param location string

@description('Resource name prefix')
param namePrefix string

var vnetName = '${namePrefix}-vnet'

resource vnet 'Microsoft.Network/virtualNetworks@2023-11-01' = {
  name: vnetName
  location: location
  properties: {
    addressSpace: {
      addressPrefixes: ['10.0.0.0/16']
    }
    subnets: [
      {
        name: 'snet-app'
        properties: {
          addressPrefix: '10.0.1.0/24'
          delegations: [
            {
              name: 'app-service-delegation'
              properties: {
                serviceName: 'Microsoft.Web/serverFarms'
              }
            }
          ]
        }
      }
      {
        name: 'snet-data'
        properties: {
          addressPrefix: '10.0.2.0/24'
          privateEndpointNetworkPolicies: 'Disabled'
        }
      }
      {
        name: 'snet-keyvault'
        properties: {
          addressPrefix: '10.0.3.0/24'
          privateEndpointNetworkPolicies: 'Disabled'
        }
      }
      {
        name: 'snet-vm'
        properties: {
          addressPrefix: '10.0.4.0/24'
        }
      }
      {
        name: 'AzureFirewallSubnet'
        properties: {
          addressPrefix: '10.0.5.0/26'
        }
      }
      {
        name: 'AzureBastionSubnet'
        properties: {
          addressPrefix: '10.0.6.0/26'
        }
      }
      {
        name: 'snet-gateway'
        properties: {
          addressPrefix: '10.0.7.0/24'
        }
      }
    ]
  }
}

output vnetName string = vnet.name
output appSubnetId string = vnet.properties.subnets[0].id
output dataSubnetId string = vnet.properties.subnets[1].id
output keyVaultSubnetId string = vnet.properties.subnets[2].id
output vmSubnetId string = vnet.properties.subnets[3].id
output firewallSubnetId string = vnet.properties.subnets[4].id
output bastionSubnetId string = vnet.properties.subnets[5].id
output gatewaySubnetId string = vnet.properties.subnets[6].id
