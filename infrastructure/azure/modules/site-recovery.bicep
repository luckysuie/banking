param location string
param namePrefix string
param linuxVmId string
param windowsVmId string

var recoveryVaultName = '${namePrefix}-rsv'

resource recoveryVault 'Microsoft.RecoveryServices/vaults@2023-04-01' = {
  name: recoveryVaultName
  location: location
  sku: {
    name: 'RS0'
    tier: 'Standard'
  }
  properties: {}
}

resource replicationPolicy 'Microsoft.RecoveryServices/vaults/replicationPolicies@2023-04-01' = {
  parent: recoveryVault
  name: 'cloudbank-replication-policy'
  properties: {
    providerSpecificInput: {
      instanceType: 'A2A'
    }
  }
}

resource fabric 'Microsoft.RecoveryServices/vaults/replicationFabrics@2023-04-01' = {
  parent: recoveryVault
  name: 'asr-fabric'
  properties: {}
}

output recoveryVaultName string = recoveryVault.name
output replicationPolicyId string = replicationPolicy.id
