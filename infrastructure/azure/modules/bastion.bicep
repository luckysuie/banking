param location string
param namePrefix string
param bastionSubnetId string

var bastionName = '${namePrefix}-bastion'
var bastionPipName = '${namePrefix}-bastion-pip'

resource bastionPublicIp 'Microsoft.Network/publicIPAddresses@2023-11-01' = {
  name: bastionPipName
  location: location
  sku: { name: 'Standard' }
  properties: {
    publicIPAllocationMethod: 'Static'
  }
}

resource bastionHost 'Microsoft.Network/bastionHosts@2023-11-01' = {
  name: bastionName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    ipConfigurations: [
      {
        name: 'bastion-ip-config'
        properties: {
          subnet: { id: bastionSubnetId }
          publicIPAddress: { id: bastionPublicIp.id }
        }
      }
    ]
  }
}

output bastionHostName string = bastionHost.name
