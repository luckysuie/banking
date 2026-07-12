param location string
param namePrefix string
param gatewaySubnetId string
param apiAppHostname string
param webAppHostname string

var publicIpName = '${namePrefix}-agw-pip'
var appGatewayName = '${namePrefix}-agw'

resource publicIp 'Microsoft.Network/publicIPAddresses@2023-11-01' = {
  name: publicIpName
  location: location
  sku: {
    name: 'Standard'
  }
  properties: {
    publicIPAllocationMethod: 'Static'
  }
}

resource appGateway 'Microsoft.Network/applicationGateways@2023-11-01' = {
  name: appGatewayName
  location: location
  properties: {
    sku: {
      name: 'WAF_v2'
      tier: 'WAF_v2'
      capacity: 2
    }
    gatewayIPConfigurations: [
      {
        name: 'gateway-ip-config'
        properties: {
          subnet: {
            id: gatewaySubnetId
          }
        }
      }
    ]
    frontendIPConfigurations: [
      {
        name: 'frontend-ip'
        properties: {
          publicIPAddress: {
            id: publicIp.id
          }
        }
      }
    ]
    frontendPorts: [
      { name: 'port-443', properties: { port: 443 } }
      { name: 'port-80', properties: { port: 80 } }
    ]
    backendAddressPools: [
      {
        name: 'api-pool'
        properties: {
          backendAddresses: [
            { fqdn: apiAppHostname }
          ]
        }
      }
      {
        name: 'web-pool'
        properties: {
          backendAddresses: [
            { fqdn: webAppHostname }
          ]
        }
      }
    ]
    backendHttpSettingsCollection: [
      {
        name: 'api-settings'
        properties: {
          port: 443
          protocol: 'Https'
          cookieBasedAffinity: 'Disabled'
          pickHostNameFromBackendAddress: true
          requestTimeout: 60
        }
      }
      {
        name: 'web-settings'
        properties: {
          port: 443
          protocol: 'Https'
          cookieBasedAffinity: 'Disabled'
          pickHostNameFromBackendAddress: true
          requestTimeout: 60
        }
      }
    ]
    httpListeners: [
      {
        name: 'api-listener'
        properties: {
          frontendIPConfiguration: { id: resourceId('Microsoft.Network/applicationGateways/frontendIPConfigurations', appGatewayName, 'frontend-ip') }
          frontendPort: { id: resourceId('Microsoft.Network/applicationGateways/frontendPorts', appGatewayName, 'port-443') }
          protocol: 'Https'
          sslCertificate: null
        }
      }
    ]
    requestRoutingRules: [
      {
        name: 'api-rule'
        properties: {
          ruleType: 'Basic'
          priority: 100
          httpListener: { id: resourceId('Microsoft.Network/applicationGateways/httpListeners', appGatewayName, 'api-listener') }
          backendAddressPool: { id: resourceId('Microsoft.Network/applicationGateways/backendAddressPools', appGatewayName, 'api-pool') }
          backendHttpSettings: { id: resourceId('Microsoft.Network/applicationGateways/backendHttpSettingsCollection', appGatewayName, 'api-settings') }
        }
      }
    ]
    webApplicationFirewallConfiguration: {
      enabled: true
      firewallMode: 'Prevention'
      ruleSetType: 'OWASP'
      ruleSetVersion: '3.2'
    }
    sslPolicy: {
      policyType: 'Predefined'
      policyName: '20220101'
    }
  }
}

output appGatewayPublicIp string = publicIp.properties.ipAddress
