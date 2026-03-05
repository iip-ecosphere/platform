# Virtual testing/twinning

If the real device/machine is not accessible as needed or you want to test an oktoflow connector/app prior to testing it on-site, a **virtual/simulated counterpart** could be helpful. While we focus on just providing a counterpart the offers an expected situation, a more precise approach might use a **virtual twin**.

## OPC UA: Create simplified OPC UA simulation using a Docker container.

###	Introduction

-	We use a Docker image provided by [IOTech Systems](https://iotechsys.com/)
-	The guide to use the simulation Docker container is the [simulator overview](https://docs.iotechsys.com/edge-xrt21/simulators/opc-ua/simulator/overview.html)
-	To browse the OPC UA, we use another Docker image provided by IOTech Systems, the [opcua browser](https://docs.iotechsys.com/edge-opcuabrowser10/installation/installation.html). 

### Brief instructions to use the container (without security authentication): 
- To run the container simulation, use the following command:
`docker run --rm --name opc-ua-sim -p 49947:49947 iotechsys/opc-ua-sim:1.2 -l /example-scripts/simulation.lua`
- To run the container browser, use the following command:
`docker run -d --name opc-ua-browser -p 8080:8080 iotechsys/opc-ua-browser`
- Open your web browser on http://localhost:8080
- Open this `url opc.tcp://xxx.xxx.xxx.xxx:49947/`, where `xxx.xxx.xxx.xxx` is your IP address.

### Customizing the OPC UA structure

The instruction above generates a default example provided by IOTech Systems. To use it with different structure/data, you need to create a custom simulation Lua script. For creating and using a custom simulation Lua script, please follow the [tutorial and example](https://docs.iotechsys.com/edge-xrt21/simulators/opc-ua/simulator/lua-scripting.html). This is a brief summary:
- Create a folder in the current directory with the name ``lua-scripts`` and put the custom simulation Lua script in it.
- To run OPC UA simulation with a custom simulation Lua script, use the following command (without security authentication):
`docker run --rm --name opc-ua-sim -p 49947:49947 -v $(pwd)/lua-scripts/:/docker-lua-scripts/ iotechsys/opc-ua-sim:1.2 -l /docker-lua-scripts/custom.script.lua` where `custom.script.lua` is the name of a custom simulation Lua script and `$(pwd)` is a  Linux command to obtain the current directory (`%cd%` on Windows).

### Example for the ReGaP virtual sensor app

Use this [custom simulation Lua script](examples/custom.script.lua), here are the key points of the custom simulation Lua script:
-	Create a variable `actSpeed` with `Double` type
```lua
    actSpeed_variant = Variant.new(DataType.DOUBLE)
    actSpeed_variant:setScalar(0)
```
-	Add `Sinumerik` folder to the `Objects` folder
```lua
Sinumerik_folder = ObjectNode.newRootFolder("Sinumerik", ns2) 
Server.addObjectNode (Sinumerik_folder)
```
-	Add `Channel` folder to the `Sinumerik` folder
```lua
Channel_folder = Sinumerik_folder.newFolder("Channel", ns2, Sinumerik_folder:getNodeId()) 
Sinumerik_folder.addObjectNode (Channel_folder)
```
-	Add `Spindle` folder to the `Channel` folder
```lua
Spindle_folder = Channel_folder.newFolder("Spindle", ns2, Channel_folder:getNodeId()) 
Channel_folder.addObjectNode (Spindle_folder)
```
-	Add `actSpeed` variable to the `Spindle` folder
```lua
actSpeed = VariableNode.new(NodeId.newString("actSpeed",ns2), "actSpeed", Spindle_folder:getNodeId(), actSpeed_variant, AccessLevel.READ)
Server.addVariableNode (actSpeed)
```
-	Update the `actSpeed` variable value periodically 
```lua
actSpeed_variant:setScalar(actSpeed_variant:getScalar() + 1)
actSpeed:updateValue()
```
