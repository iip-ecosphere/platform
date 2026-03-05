-- Helper functions
last_update = os.time()
wait_time = 1 --seconds

t = 0;

--helper functions 
function get_elapsed()
  current = os.time()
  elapsed =  current - last_update
  return elapsed
end 

function should_update()
  if get_elapsed() < wait_time then
    return false
  end
  last_update = os.time()
  return true
end

--Create a new namespace
ns = Server.addNamespace("Static")

-- Create a variable actSpeed with a type
actSpeed_variant = Variant.new(DataType.DOUBLE)
actSpeed_variant:setScalar(0)

function add_Sinumerik_nodes()

  -- Creates namespace 2
  ns2 = Server.addNamespace("Sinumerik") 

  -- Add Sinumerik_folder to the Objects folder
  Sinumerik_folder = ObjectNode.newRootFolder("Sinumerik", ns2) 
  Server.addObjectNode (Sinumerik_folder)

  -- Add Channel_folder to the Sinumerik_folder
  Channel_folder = Sinumerik_folder.newFolder("Channel", ns2, Sinumerik_folder:getNodeId()) 
  Sinumerik_folder.addObjectNode (Channel_folder)

  -- Add Spindle_folder to the Channel_folder
  Spindle_folder = Channel_folder.newFolder("Spindle", ns2, Channel_folder:getNodeId()) 
  Channel_folder.addObjectNode (Spindle_folder)

  -- Add actSpeed variable to the Spindle_folder
  actSpeed = VariableNode.new(NodeId.newString("actSpeed",ns2), "actSpeed", Spindle_folder:getNodeId(), actSpeed_variant, AccessLevel.READ)
  Server.addVariableNode (actSpeed)

end

add_Sinumerik_nodes()

-- update the nodes values every 1 second
function Update()

  if not should_update() then
    return
  end
  t = t + 5

  -- Update the actSpeed variable value periodically 
  actSpeed_variant:setScalar(actSpeed_variant:getScalar() + 1)
  actSpeed:updateValue()

end
