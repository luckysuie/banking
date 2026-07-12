param location string
param namePrefix string
param vmSubnetId string

var linuxVmName = '${namePrefix}-linux-vm'
var windowsVmName = '${namePrefix}-win-vm'
var nsgName = '${namePrefix}-vm-nsg'

@secure()
param adminPassword string = 'VmChangeMe-${uniqueString(subscription().id)}!'

resource nsg 'Microsoft.Network/networkSecurityGroups@2023-11-01' = {
  name: nsgName
  location: location
  properties: {
    securityRules: [
      {
        name: 'DenyAllInbound'
        properties: {
          priority: 4096
          direction: 'Inbound'
          access: 'Deny'
          protocol: '*'
          sourceAddressPrefix: '*'
          sourcePortRange: '*'
          destinationAddressPrefix: '*'
          destinationPortRange: '*'
        }
      }
    ]
  }
}

resource linuxNic 'Microsoft.Network/networkInterfaces@2023-11-01' = {
  name: '${linuxVmName}-nic'
  location: location
  properties: {
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: { id: vmSubnetId }
          privateIPAllocationMethod: 'Dynamic'
        }
      }
    ]
    networkSecurityGroup: { id: nsg.id }
  }
}

resource windowsNic 'Microsoft.Network/networkInterfaces@2023-11-01' = {
  name: '${windowsVmName}-nic'
  location: location
  properties: {
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: { id: vmSubnetId }
          privateIPAllocationMethod: 'Dynamic'
        }
      }
    ]
    networkSecurityGroup: { id: nsg.id }
  }
}

resource linuxVm 'Microsoft.Compute/virtualMachines@2023-09-01' = {
  name: linuxVmName
  location: location
  properties: {
    hardwareProfile: { vmSize: 'Standard_B2s' }
    osProfile: {
      computerName: linuxVmName
      adminUsername: 'azureuser'
      adminPassword: adminPassword
      linuxConfiguration: {
        disablePasswordAuthentication: false
      }
    }
    storageProfile: {
      imageReference: {
        publisher: 'Canonical'
        offer: '0001-com-ubuntu-server-jammy'
        sku: '22_04-lts-gen2'
        version: 'latest'
      }
      osDisk: {
        createOption: 'FromImage'
        managedDisk: { storageAccountType: 'Standard_LRS' }
      }
    }
    networkProfile: {
      networkInterfaces: [{ id: linuxNic.id }]
    }
  }
}

resource windowsVm 'Microsoft.Compute/virtualMachines@2023-09-01' = {
  name: windowsVmName
  location: location
  properties: {
    hardwareProfile: { vmSize: 'Standard_B2s' }
    osProfile: {
      computerName: windowsVmName
      adminUsername: 'azureadmin'
      adminPassword: adminPassword
      windowsConfiguration: {
        enableAutomaticUpdates: true
      }
    }
    storageProfile: {
      imageReference: {
        publisher: 'MicrosoftWindowsServer'
        offer: 'WindowsServer'
        sku: '2022-Datacenter'
        version: 'latest'
      }
      osDisk: {
        createOption: 'FromImage'
        managedDisk: { storageAccountType: 'Standard_LRS' }
      }
    }
    networkProfile: {
      networkInterfaces: [{ id: windowsNic.id }]
    }
  }
}

output linuxVmId string = linuxVm.id
output windowsVmId string = windowsVm.id
