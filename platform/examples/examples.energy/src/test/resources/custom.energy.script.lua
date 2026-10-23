-- custom.energy.script.lua
-- Energy simulator for generated Energy connector validation.
-- Exposes the concrete Energy measurement values expected by energy.ivml.

-- Helper variables
last_update = os.time()
wait_time = 1 -- seconds

-- Initial simulated Energy values
neEnergyImportHpValue = 100.0
neEnergyExportHpValue = 20.0

pressureValue = 5.0
temperatureValue = 22.0

volumeValue = 10.0
volumeFlowRateValue = 2.0

massValue = 15.0
massFlowRateValue = 3.0

-- Helper functions
function get_elapsed()
  current = os.time()
  elapsed = current - last_update
  return elapsed
end

function should_update()
  if get_elapsed() < wait_time then
    return false
  end

  last_update = os.time()
  return true
end

-- Create namespace
ns = Server.addNamespace("Static")

-- Create OPC UA value variants

-- NonElectricalEnergy values are DoubleType in OpcEnergy.ivml
NeEnergyImportHp_variant = Variant.new(DataType.DOUBLE)
NeEnergyImportHp_variant:setScalar(neEnergyImportHpValue)

NeEnergyExportHp_variant = Variant.new(DataType.DOUBLE)
NeEnergyExportHp_variant:setScalar(neEnergyExportHpValue)

-- Remaining Energy measurements are FloatType in OpcEnergy.ivml
Pressure_variant = Variant.new(DataType.FLOAT)
Pressure_variant:setScalar(pressureValue)

Temperature_variant = Variant.new(DataType.FLOAT)
Temperature_variant:setScalar(temperatureValue)

Volume_variant = Variant.new(DataType.FLOAT)
Volume_variant:setScalar(volumeValue)

VolumeFlowRate_variant = Variant.new(DataType.FLOAT)
VolumeFlowRate_variant:setScalar(volumeFlowRateValue)

Mass_variant = Variant.new(DataType.FLOAT)
Mass_variant:setScalar(massValue)

MassFlowRate_variant = Variant.new(DataType.FLOAT)
MassFlowRate_variant:setScalar(massFlowRateValue)


function add_Energy_nodes()

  -- Reuse Sinumerik as the root used by the validation setup
  ns2 = Server.addNamespace("Sinumerik")

  -- Objects/Sinumerik
  Sinumerik_folder = ObjectNode.newRootFolder("Sinumerik", ns2)
  Server.addObjectNode(Sinumerik_folder)

  -- Objects/Sinumerik/Energy
  Energy_folder = Sinumerik_folder.newFolder(
    "Energy",
    ns2,
    Sinumerik_folder:getNodeId()
  )
  Sinumerik_folder.addObjectNode(Energy_folder)

  -- Objects/Sinumerik/Energy/NeEnergyImportHp
  NeEnergyImportHp = VariableNode.new(
    NodeId.newString("NeEnergyImportHp", ns2),
    "NeEnergyImportHp",
    Energy_folder:getNodeId(),
    NeEnergyImportHp_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(NeEnergyImportHp)

  -- Objects/Sinumerik/Energy/NeEnergyExportHp
  NeEnergyExportHp = VariableNode.new(
    NodeId.newString("NeEnergyExportHp", ns2),
    "NeEnergyExportHp",
    Energy_folder:getNodeId(),
    NeEnergyExportHp_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(NeEnergyExportHp)

  -- Objects/Sinumerik/Energy/Pressure
  Pressure = VariableNode.new(
    NodeId.newString("Pressure", ns2),
    "Pressure",
    Energy_folder:getNodeId(),
    Pressure_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(Pressure)

  -- Objects/Sinumerik/Energy/Temperature
  Temperature = VariableNode.new(
    NodeId.newString("Temperature", ns2),
    "Temperature",
    Energy_folder:getNodeId(),
    Temperature_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(Temperature)

  -- Objects/Sinumerik/Energy/Volume
  Volume = VariableNode.new(
    NodeId.newString("Volume", ns2),
    "Volume",
    Energy_folder:getNodeId(),
    Volume_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(Volume)

  -- Objects/Sinumerik/Energy/VolumeFlowRate
  VolumeFlowRate = VariableNode.new(
    NodeId.newString("VolumeFlowRate", ns2),
    "VolumeFlowRate",
    Energy_folder:getNodeId(),
    VolumeFlowRate_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(VolumeFlowRate)

  -- Objects/Sinumerik/Energy/Mass
  Mass = VariableNode.new(
    NodeId.newString("Mass", ns2),
    "Mass",
    Energy_folder:getNodeId(),
    Mass_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(Mass)

  -- Objects/Sinumerik/Energy/MassFlowRate
  MassFlowRate = VariableNode.new(
    NodeId.newString("MassFlowRate", ns2),
    "MassFlowRate",
    Energy_folder:getNodeId(),
    MassFlowRate_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(MassFlowRate)

end

add_Energy_nodes()


-- Update simulated values every second
function Update()

  if not should_update() then
    return
  end

  neEnergyImportHpValue = neEnergyImportHpValue + 1.0
  neEnergyExportHpValue = neEnergyExportHpValue + 0.5

  pressureValue = pressureValue + 0.1
  temperatureValue = temperatureValue + 0.1

  volumeValue = volumeValue + 0.2
  volumeFlowRateValue = volumeFlowRateValue + 0.1

  massValue = massValue + 0.2
  massFlowRateValue = massFlowRateValue + 0.1

  -- Keep values within simple validation ranges
  if neEnergyImportHpValue > 150.0 then
    neEnergyImportHpValue = 100.0
  end

  if neEnergyExportHpValue > 40.0 then
    neEnergyExportHpValue = 20.0
  end

  if pressureValue > 10.0 then
    pressureValue = 5.0
  end

  if temperatureValue > 30.0 then
    temperatureValue = 22.0
  end

  if volumeValue > 20.0 then
    volumeValue = 10.0
  end

  if volumeFlowRateValue > 5.0 then
    volumeFlowRateValue = 2.0
  end

  if massValue > 25.0 then
    massValue = 15.0
  end

  if massFlowRateValue > 6.0 then
    massFlowRateValue = 3.0
  end

  -- Update OPC UA values
  NeEnergyImportHp_variant:setScalar(neEnergyImportHpValue)
  NeEnergyExportHp_variant:setScalar(neEnergyExportHpValue)

  Pressure_variant:setScalar(pressureValue)
  Temperature_variant:setScalar(temperatureValue)

  Volume_variant:setScalar(volumeValue)
  VolumeFlowRate_variant:setScalar(volumeFlowRateValue)

  Mass_variant:setScalar(massValue)
  MassFlowRate_variant:setScalar(massFlowRateValue)

  NeEnergyImportHp:updateValue()
  NeEnergyExportHp:updateValue()

  Pressure:updateValue()
  Temperature:updateValue()

  Volume:updateValue()
  VolumeFlowRate:updateValue()

  Mass:updateValue()
  MassFlowRate:updateValue()

end

