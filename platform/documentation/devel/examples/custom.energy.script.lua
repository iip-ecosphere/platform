-- custom.energy.script.lua
-- Initial Lua-based OPC UA Energy Aggregator prototype
-- Based on the existing oktoflow custom.script.lua example.

-- Helper variables
last_update = os.time()
wait_time = 1 -- seconds

-- Initial simulated Energy values
nonElectricalEnergyValue = 100.0
baseFlowValue = 20.0
volumeFlowValue = 10.0
massFlowValue = 5.0

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

-- Create a namespace
ns = Server.addNamespace("Static")

-- Create value variants
NonElectricalEnergy_variant = Variant.new(DataType.DOUBLE)
NonElectricalEnergy_variant:setScalar(nonElectricalEnergyValue)

BaseFlow_variant = Variant.new(DataType.DOUBLE)
BaseFlow_variant:setScalar(baseFlowValue)

VolumeFlow_variant = Variant.new(DataType.DOUBLE)
VolumeFlow_variant:setScalar(volumeFlowValue)

MassFlow_variant = Variant.new(DataType.DOUBLE)
MassFlow_variant:setScalar(massFlowValue)

function add_Energy_nodes()

  -- Create namespace for the reused Sinumerik root
  ns2 = Server.addNamespace("Sinumerik")

  -- Add Sinumerik folder to the Objects folder
  Sinumerik_folder = ObjectNode.newRootFolder("Sinumerik", ns2)
  Server.addObjectNode(Sinumerik_folder)

  -- Add Energy folder to the Sinumerik folder
  Energy_folder = Sinumerik_folder.newFolder("Energy", ns2, Sinumerik_folder:getNodeId())
  Sinumerik_folder.addObjectNode(Energy_folder)

  -- Add NonElectricalEnergy variable to the Energy folder
  NonElectricalEnergy = VariableNode.new(
    NodeId.newString("NonElectricalEnergy", ns2),
    "NonElectricalEnergy",
    Energy_folder:getNodeId(),
    NonElectricalEnergy_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(NonElectricalEnergy)

  -- Add BaseFlow variable to the Energy folder
  BaseFlow = VariableNode.new(
    NodeId.newString("BaseFlow", ns2),
    "BaseFlow",
    Energy_folder:getNodeId(),
    BaseFlow_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(BaseFlow)

  -- Add VolumeFlow variable to the Energy folder
  VolumeFlow = VariableNode.new(
    NodeId.newString("VolumeFlow", ns2),
    "VolumeFlow",
    Energy_folder:getNodeId(),
    VolumeFlow_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(VolumeFlow)

  -- Add MassFlow variable to the Energy folder
  MassFlow = VariableNode.new(
    NodeId.newString("MassFlow", ns2),
    "MassFlow",
    Energy_folder:getNodeId(),
    MassFlow_variant,
    AccessLevel.READ
  )
  Server.addVariableNode(MassFlow)

end

add_Energy_nodes()

-- Update node values every 1 second
function Update()
  if not should_update() then
    return
  end

  -- Simple simulated value changes for initial prototype only
  nonElectricalEnergyValue = nonElectricalEnergyValue + 1.0
  baseFlowValue = baseFlowValue + 0.5
  volumeFlowValue = volumeFlowValue + 0.2
  massFlowValue = massFlowValue + 0.1

  -- Reset values to keep them within a simple test range
  if nonElectricalEnergyValue > 150.0 then
    nonElectricalEnergyValue = 100.0
  end

  if baseFlowValue > 30.0 then
    baseFlowValue = 20.0
  end

  if volumeFlowValue > 15.0 then
    volumeFlowValue = 10.0
  end

  if massFlowValue > 8.0 then
    massFlowValue = 5.0
  end

  -- Update OPC UA variable values
  NonElectricalEnergy_variant:setScalar(nonElectricalEnergyValue)
  BaseFlow_variant:setScalar(baseFlowValue)
  VolumeFlow_variant:setScalar(volumeFlowValue)
  MassFlow_variant:setScalar(massFlowValue)

  NonElectricalEnergy:updateValue()
  BaseFlow:updateValue()
  VolumeFlow:updateValue()
  MassFlow:updateValue()

end

