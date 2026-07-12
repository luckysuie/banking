param dnsZoneName string
param frontDoorEndpointHostname string

resource dnsZone 'Microsoft.Network/dnsZones@2018-05-01' = {
  name: dnsZoneName
  location: 'global'
}

resource aRecord 'Microsoft.Network/dnsZones/A@2018-05-01' = {
  parent: dnsZone
  name: '@'
  properties: {
    TTL: 3600
    targetResource: {
      id: resourceId('Microsoft.Cdn/profiles/afdEndpoints', 'placeholder', 'placeholder')
    }
  }
}

resource cnameWww 'Microsoft.Network/dnsZones/CNAME@2018-05-01' = {
  parent: dnsZone
  name: 'www'
  properties: {
    TTL: 3600
    CNAMERecord: {
      cname: frontDoorEndpointHostname
    }
  }
}

resource cnameApp 'Microsoft.Network/dnsZones/CNAME@2018-05-01' = {
  parent: dnsZone
  name: 'banking'
  properties: {
    TTL: 3600
    CNAMERecord: {
      cname: frontDoorEndpointHostname
    }
  }
}

output dnsZoneName string = dnsZone.name
