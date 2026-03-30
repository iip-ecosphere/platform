# Virtual OPC-UA Connector

This guide explains how to simulate an OPC-UA endpoint and connect to it locally.

If the physical device/machine is unavailable — or you want to validate an oktoflow connector/app before going on-site — a **virtual/simulated counterpart** provides a practical testing environment. Note that this is a lightweight simulation that replicates expected behavior, not a full **digital twin**.

## OPC UA: Create simplified OPC UA simulation using a Docker containers.

-	The OPC-UA endpoint and browser are provided by [IOTech Systems](https://iotechsys.com/) as Docker images.
-	Docker images by IOTech Systems are free for academic purposes (non-commercial use).
-	In addition to this documentation, refer to the official documentation: [OPC-UA Simulator](https://docs.iotechsys.com/edge-xrt21/simulators/opc-ua/simulator/overview.html) and [OPC-UA Browser](https://docs.iotechsys.com/edge-opcuabrowser10/installation/installation.html).

### Simulator (without security authentication): 

The OPC-UA simulator replicates a machine OPC-UA endpoint whose behavior is defined through a Lua script.

Running a Docker container from the OPC-UA image with the pre-defined structure uses an example configuration (Provided by IOTech Systems):
```bash
docker run --rm --name opc-ua-sim -p 49947:49947 iotechsys/opc-ua-sim:1.2 -l /example-scripts/simulation.lua
```

For Lua customization beyond the example, see the official [tutorial and examples](https://docs.iotechsys.com/edge-xrt21/simulators/opc-ua/simulator/lua-scripting.html).

### Browser

The browser image visualizes an OPC-UA endpoint in the web browser.

1. Running a Docker container from the browser image:
```bash
docker run -d --name opc-ua-browser -p 8080:8080 iotechsys/opc-ua-browser
```

2. Navigate to [http://localhost:8080](http://localhost:8080) in your web browser.

3. Create a new connection to the simulator to inspect the OPC-UA endpoint. Enter the following connection URL:
```
opc.tcp://xxx.xxx.xxx.xxx:49947/
```
Replace `xxx.xxx.xxx.xxx` with your local IP address (`hostname -I` on Linux, `ipconfig` on Windows). The port `49947` is defined by the Docker run command and must match the port used in the IVML connector definition.

### Customized OPC UA structure

To use it with different structure/data, you need to create a custom simulation Lua script. For creating and using a custom simulation Lua script, please follow the [tutorial and example](https://docs.iotechsys.com/edge-xrt21/simulators/opc-ua/simulator/lua-scripting.html). This is a brief summary:
1. Create a folder named `docker-lua-scripts` in the current directory and place the [custom simulation Lua script](examples/custom.script.lua) inside it.
2. Running a Docker container from OPC-UA image with the custom Lua script `custom.script.lua` by mounting the script folder (`$(pwd)` is `%cd%` on Windows):

```bash
docker run --rm --name opc-ua-sim -p 49947:49947 -v $(pwd)/docker-lua-scripts/:/docker-lua-scripts/ iotechsys/opc-ua-sim:1.2 -l /docker-lua-scripts/custom.script.lua
```

#### Lua Script Structure
The main components of the custom Lua script responsible for creating the structure and variables are as follows:

-	Declare an `actSpeed` variable of type `Double`:
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
-	Periodically update the `actSpeed` variable value: 
```lua
actSpeed_variant:setScalar(actSpeed_variant:getScalar() + 1)
actSpeed:updateValue()
```

## IVML connector and type definition

To connect an application to the customized OPC-UA above, use the following IVML connector and type

-	The output type for the OPC UA connector
```ivml
RecordType OpCuaOutput = {
  name = "OpCuaOutput",
  fields = {
    Field {
      name = "actSpeed",
      type = refBy(DoubleType),
      cachingTime=CACHE_NONE
    }
  }
};
```

-	The OPC UA connector
```ivml
OpcUaV1Connector myOpcUaConn = {
  id = "myOpcUaConn",
  name = "myOpcUaConn",
  description = "This is the OPC UA connector.",
  ver = "0.1.0",
  host = "xxx.xxx.xxx.xxx", //local IP address
  port = 49947,
  samplingPeriod = 1000,
  kind = ServiceKind::SOURCE_SERVICE,
  input = {{type=refBy(EmptyRecord)}},
  output = {{type=refBy(OpCuaOut)}},
  inInterface = {{type=refBy(EmptyRecord)}},
  outInterface = {{type=refBy(OpCuaOut), path="Objects/Sinumerik/Channel/Spindle/"}}
};
```

