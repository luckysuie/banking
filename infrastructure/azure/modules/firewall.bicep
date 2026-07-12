param location string
param namePrefix string
param vnetName string
param firewallSubnetId string

var firewallName = '${namePrefix}-fw'
var firewallPipName = '${namePrefix}-fw-pip'

resource firewallPublicIp 'Microsoft.Network/publicIPAddresses@2023-11-01' = {
  name: firewallPipName
  location: location
  sku: { name: 'Standard' }
  properties: {
    publicIPAllocationMethod: 'Static'
  }
}

resource firewall 'Microsoft.Network/azureFirewalls@2023-11-01' = {
  name: firewallName
  location: location
  properties: {
    sku: {
      name: 'AZFW_VNet'
      tier: 'Standard'
    }
    ipConfigurations: [
      {
        name: 'firewall-config'
        properties: {
          subnet: { id: firewallSubnetId }
          publicIPAddress: { id: firewallPublicIp.id }
        }
      }
    ]
    networkRuleCollections: [
      {
        name: 'vm-egress-rules'
        properties: {
          priority: 100
          action: { type: 'Allow' }
          rules: [
            {
              name: 'allow-https'
              protocols: ['TCP']
              sourceAddresses: ['10.0.4.0/24']
              destinationAddresses: ['*']
              destinationPorts: ['443']
            }
            {
              name: 'allow-dns'
              protocols: ['UDP']
              sourceAddresses: ['10.0.4.0/24']
              destinationAddresses: ['*']
              destinationPorts: ['53']
            }
          ]
        }
      }
    ]
  }
}

resource routeTable 'Microsoft.Network/routeTables@2023-11-01' = {
  name: '${namePrefix}-vm-routes'
  location: location
  properties: {
    disableBgpRoutePropagation: false
    routes: [
      {
        name: 'default-via-firewall'
        properties: {
          addressPrefix: '0.0.0.0/0'
          nextHopType: 'VirtualAppliance'
          nextHopIpAddress: firewall.properties.ipConfigurations[0].properties.privateIPAddress
        }
      }
    ]
  }
}

output firewallPrivateIp string = firewall.properties.ipConfigurations[0].properties.privateIPAddress
