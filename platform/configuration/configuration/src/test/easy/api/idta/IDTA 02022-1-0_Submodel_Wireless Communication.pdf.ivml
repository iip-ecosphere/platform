project IDTA_02022_WirelessCommunication {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType WirelessCommunication = {
    name = "WirelessCommunication",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/1/0",
    description = "Contains the wireless communication aspects of the asset.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "WirelessCommunicationFunction",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/1/0",
        type = refBy(WirelessCommunicationFunction),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Hardware and software implementation of algorithms for wireless communication (VDI/VDE 2185 Part-4, 2019).  Describes the parameters of the wireless communication function."
      },
      AasField {
        name = "WirelessNetworkRole",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessNetworkRole/1/0",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Describes the role of the asset in the network: Access point, end node, repeater. It could also be an immaterial asset as a heatmap, or a wireless communication manager."
      },
      AasField {
        name = "RadioFrequency",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/1/0",
        type = refBy(RadioFrequency),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "It describes the characteristics of radio channel as frequency, band and bandwidth."
      },
      AasField {
        name = "ReceptionQuality",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQuality/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        description = "Set of parameters that describe the quality of reception. The availabitly of this parameter depends on the technology and implementations."
      },
      AasField {
        name = "OutputPower",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/OutputPower/1/0",
        type = refBy(OutputPower),
        minimumInstances = 0,
        description = "The transmit power is determined by the RF schematic of the wireless module. Depending on the wireless technology, standard, or implementation it is fixed, adjustable in steps or free configurable (VID/VDE 2185 Part4)."
      },
      AasField {
        name = "Hardware",
        semanticId = "iri:https://adminshell.io/idta/WirelessCommunication/Hardware/1/0",
        type = refBy(Hardware),
        minimumInstances = 0,
        description = "A Set of parameters that describe hardware aspects of the asset, such as antenna type, connectors, and interfaces, during different life cycle phases."
      },
      AasField {
        name = "Authentification",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        description = "https://adminshell.io/idta/WirelessCommunication/Authentication/1/0 It contains information needed for device authentication. It is required to verify the legitimacy of devices and ensure secure network access."
      },
      AasField {
        name = "License",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/1/0",
        type = refBy(License),
        minimumInstances = 0,
        description = "It contains parameters related to licensing, which are utilized by the regulatory authority when applying for a license. Certain parameters found within this SMC may also appear in other SMCs, resulting in duplication."
      }
    }
  };

  AasSubmodelElementCollectionType WirelessCommunicationFunction = {
    name = "WirelessCommunicationFunction",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/1/0",
    description = "Hardware and software implementation of algorithms for wireless communication (VDI/VDE 2185 Part-4, 2019).  Describes the parameters of the wireless communication function.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "TechnologyStandard",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/TechnologyStandard/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5G NR","Rel.15","802.11ac"},
        description = "It includes the name of the technology and the release/version."
      },
      AasField {
        name = "CommunicationCycle",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/CommunicationCycle/1/0",
        type = refBy(IntegerListType),
        minimumInstances = 0,
        description = "It describes the cycle with which the communication stack executes requests from the application and the wireless medium in ms."
      },
      AasField {
        name = "MediumAccess",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/1/0",
        type = refBy(MediumAccess),
        minimumInstances = 0,
        description = "The media access control ensures, for example, that a communication request is served as long as the medium is free (CSMA) or it allocates the request to well-defined time slots (TDMA)."
      },
      AasField {
        name = "SecurityMechanism",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/SecurityMechanism/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "It specifies how the security objectives of the application are met by the implemented security mechanisms."
      }
    }
  };

  AasSubmodelElementCollectionType MediumAccess = {
    name = "MediumAccess",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/1/0",
    description = "The media access control ensures, for example, that a communication request is served as long as the medium is free (CSMA) or it allocates the request to well-defined time slots (TDMA).",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "MediumAccessType",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/MediumAccessType/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "It describes which technique is used for accessing the radio medium. It can be, for example, CSMA or TDMA. Furthermore, it highly depends on the Technology."
      },
      AasField {
        name = "SlotConfiguration",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/SlotConfiguration/1/0",
        type = refBy(SlotConfiguration),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Depending on the medium access type, the slot configuration can be described as number of slots for downlink, uplink and shared slots."
      }
    }
  };

  AasSubmodelElementCollectionType SlotConfiguration = {
    name = "SlotConfiguration",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/SlotConfiguration/1/0",
    description = "Depending on the medium access type, the slot configuration can be described as the number of slots for downlink, uplink and shared slots.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "NumberOfDownlinkSlots",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/SlotConfiguration/NumberOfDownlinkSlots/1/0",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Number of timeslots reserved for downlink communication."
      },
      AasField {
        name = "NumberOfUpllinkSlots",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/SlotConfiguration/NumberOfUpllinkSlots/1/0",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Number of timeslots reserved for uplink communication."
      },
      AasField {
        name = "NumberOfSharedSlots",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/WirelessCommunicationFunction/MediumAccess/SlotConfiguration/NumberOfSharedSlots/1/0",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Number of timeslots that are reserved for downlink and uplink communication."
      }
    }
  };

  AasSubmodelElementCollectionType RadioFrequency = {
    name = "RadioFrequency",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/1/0",
    description = "Part of the frequency band that is characterized by a lower cut-off frequency and an upper cut-off frequency, or by centre frequency and bandwidth.@en.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "FrequencyBand",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/FrequencyBand/1/0",
        type = refBy(FrequencyBand),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Contains information related to the frequency band used by the asset. It refers to a range of frequencies within the electromagnetic spectrum, which includes all frequencies of electromagnetic radiation. Different technologies and applications often use specific frequency bands for communication."
      },
      AasField {
        name = "RadioFrequencyChannel",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/RadioFrequencyChannel/1/0",
        type = refBy(RadioFrequencyChannel),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Refers to a specific channel of frequencies used for wireless communication. Radio communication technologies divide the spectrum into channels."
      }
    }
  };

  AasSubmodelElementCollectionType FrequencyBand = {
    name = "FrequencyBand",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/FrequencyBand/1/0",
    description = "Contains information related to the frequency band used by the asset. It refers to a range of frequencies within the electromagnetic spectrum, which includes all frequencies of electromagnetic radiation. Different technologies and applications often use specific frequency bands for communication.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "BandIDList",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/FrequencyBand/BandIDList/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"[n78, n43]"},
        description = "List of bands that can be configured on the asset."
      },
      AasField {
        name = "BandIDCurrent",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/FrequencyBand/BandIDCurrent/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"[n78]"},
        description = "ID of the current configured band."
      },
      AasField {
        name = "CenterFrequecy",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/FrequencyBand/CenterFrequecy/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"3700.0"},
        description = "Center frequency in MHz of the current configured band."
      },
      AasField {
        name = "Bandwidth",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/FrequencyBand/Bandwidth/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"100.0"},
        description = "Bandwidth in MHz of current configured band. Bandwidth is the difference between upper cut-off frequency and lower cut-off frequency. The bandwidth is the range of frequencies occupied by a modulated carrier signal."
      }
    }
  };

  AasSubmodelElementCollectionType RadioFrequencyChannel = {
    name = "RadioFrequencyChannel",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/RadioFrequencyChannel/1/0",
    description = "It refers to a specific frequency or range of frequencies within the frequency band that is allocated for a particular communication purpose. Multiple channels can exist within a frequency band, enabling multiple communication links to operate simultaneously without interference.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "ChannelNumbering",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/RadioFrequencyChannel/ChannelNumbering/1/0",
        type = refBy(IntegerListType),
        minimumInstances = 0,
        examples = {"14"},
        description = "Most wireless technologies assign numbers to the specified radio channels. This parameter lists the specified numbers. It may be used for some technologies (e.g., Bluetooth). Blacklisting can be considered."
      },
      AasField {
        name = "UsedChannelCentreFrequency",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/RadioFrequencyChannel/UsedChannelCentreFrequency/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Centre frequency is the geometric mean of lower cut-off frequency and  upper  cut-off  frequency  of  a  radio  channel.  This  parameter assigns the centre frequencies to the radio channels. It serves to specify   the  utilized   frequency,  crucial  for  systems  like  Wi-Fi. However, in instances of channel-hopping technologies like BLE, IOLW, and WirelessHART, where channels dynamically change, it's recommended to leave this property empty."
      },
      AasField {
        name = "UsedChannelBandwidth",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/RadioFrequency/RadioFrequencyChannel/UsedChannelBandwidth/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Bandwidth is the difference between upper cut-off frequency and lower cut-off frequency. The bandwidth is the range of frequencies occupied by a modulated carrier signal. The data  rate  of reliable communication is directly proportional to the bandwidth of the signal used for the communication."
      }
    }
  };

  AasSubmodelElementCollectionType ReceptionQualityIndicator = {
    name = "ReceptionQualityIndicator",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQualityIndicator/1/0",
    description = "A set of parameters that describe the quality of reception during different life cycle phases. The availability of these parameters depends on the implementation. It may be that not all parameters described in this section are provided/used by the manufacturer.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "ParameterType",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQualityIndicator/ParameterType/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"For 5G: SS-RSRP or RSRQ","For bluetooth RSSI or SINR"},
        description = "It is a string that describes the parameter type depending on the technology."
      },
      AasField {
        name = "Range",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQualityIndicator/Range/1/0",
        type = refBy(AasRangeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"0 to -125 dBm"},
        description = "The range delimits the limits that define the extent of acceptable values or conditions for the parameter. In the context of a reception quality indicator, one of the thresholds can typically be defined by the reception sensitivity parameter of some wireless modules."
      },
      AasField {
        name = "SpecifiedValue",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQualityIndicator/SpecifiedValue/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-85 dBm"},
        description = "It is the value specified during the design phase."
      },
      AasField {
        name = "ReferenceValue",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQualityIndicator/ReferenceValue/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-85 dBm"},
        description = "It is the value configured in the commissioning phase."
      },
      AasField {
        name = "CurrentValue",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQualityIndicator/CurrentValue/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-85 dBm"},
        description = "It is the last received signal strength value stored by the communication module."
      }
    }
  };

  AasSubmodelElementCollectionType OutputPower = {
    name = "OutputPower",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/ReceptionQuality/OutputPower/1/0",
    description = "A set of parameters that describe the Output power during the lifecycle phase. The transmit power is determined by the RF schematic of the wireless module. Depending on the wireless technology, standard or implementation it is fixed, adjustable in steps or free configurable. (VID/VDE 2185 Part4) The availability of this parameter depends on the implementations. It may be that not all parameters described in this section are provided/used by the manufacturer.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "ParameterType",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/OutputPower/ParameterType/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"EIRP"},
        description = "It is a string that describes the parameter type depending on the technology."
      },
      AasField {
        name = "Range",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/OutputPower/Range/1/0",
        type = refBy(AasRangeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"0 to -125 dBm"},
        description = "The range delimits the limits that define the extent of acceptable values or conditions for the parameter. In the context of output power, it is typically defined by the maximum transmit power of the transceiver."
      },
      AasField {
        name = "SpecifiedValue",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/OutputPower/SpecifiedValue/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-85 dBm"},
        description = "It is the value specified during the design phase. (float)."
      },
      AasField {
        name = "ReferenceValue",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/OutputPower/ReferenceValue/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-85 dBm"},
        description = "It is the value configured in the commissioning phase."
      },
      AasField {
        name = "CurrentValue",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/OutputPower/CurrentValue/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-85 dBm"},
        description = "It is the current value of the parameter."
      }
    }
  };

  AasSubmodelElementCollectionType Hardware = {
    name = "Hardware",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Hardware/1/0",
    description = "A set of parameters that describe hardware aspects of the asset, such as antenna type, connectors and interfaces, during different life cycle phases.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "AntennaType",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Hardware/AntennaType/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "It describes the antenna used for the wireless asset. (Monopole antenna, directional antenna, dipole antenna, special antenna). It also defines the antenna connector."
      },
      AasField {
        name = "NumberOfAntenas",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Hardware/NumberOfAntennas/1/0",
        type = refBy(IntegerListType),
        minimumInstances = 0,
        description = "It defines how many antennas the asset has."
      },
      AasField {
        name = "ConnectorType",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Hardware/ConnectorType/1/0",
        type = refBy(FloatListType),
        minimumInstances = 0,
        description = "Connector type of the antenna."
      },
      AasField {
        name = "AntennaHeight",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Hardware/AntennaHeight/1/0",
        type = refBy(FloatListType),
        minimumInstances = 0,
        description = "Height of the antenna."
      },
      AasField {
        name = "Interfaces",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Hardware/Interface/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "It describes the interfaces that the asset has: Ethernet, USB, Wireless, Digital IO, etc.."
      }
    }
  };

  AasSubmodelElementCollectionType Authentication = {
    name = "Authentication",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Authentication/1/0",
    description = "It contains information needed for device authentication. It is required to verify the legitimacy of devices and ensure secure network access.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "AuthenticationIdentifier",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Authentication/AuthenticationIdentifier/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "It  is  a  parameter  for  device  authentication  in  networks.  It  varies across  wireless  technologies,  serving  as  a  unique  data  piece presented  during  authentication,  such   as  5G's   'Access  Point Name,' Bluetooth's 'Master ID,' and Wi-Fi's 'SSID.'."
      },
      AasField {
        name = "AuthenticationKey",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/Authentication/AuthenticationKey/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Cryptographic codes or credentials used alongside the Authentication Identifier."
      }
    }
  };

  AasSubmodelElementCollectionType License = {
    name = "License",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/1/0",
    description = "It contains parameters related to licensing, which are utilized by the regulatory authority when applying for a licence. Certain parameters found within this SMC may also appear in other SMCs, resulting in duplication.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "Applicant",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Applicant/1/0",
        type = refBy(Applicant),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "A set of parameters that include the information about the license holder as well as the contact person."
      },
      AasField {
        name = "Frequency",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Frequency/1/0",
        type = refBy(Frequency),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "This list contains information about the used frequency or frequency ranges. Only one of the three fields must be filled in."
      },
      AasField {
        name = "Operation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Operation/1/0",
        type = refBy(Operation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "This list contains information on the period of use of the frequency ranges applied for."
      },
      AasField {
        name = "FixedStation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/1/0",
        type = refBy(FixedStation),
        minimumInstances = 0,
        description = "This list contains the geographical information of the fixed stations. Information must include both the postal and geographic address, as well as the height of the antennas."
      },
      AasField {
        name = "MobileStation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/MobileStation/1/0",
        type = refBy(MobileStation),
        minimumInstances = 0,
        description = "This list contains the information about the mobile station (used geographical area, number of mobile stations, and kind of operation)."
      },
      AasField {
        name = "LicenseAcquisitionTechnicalData",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/TechnicalData/1/0",
        type = refBy(LicenseAcquisitionTechnicalData),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "This list contains the technical parameters of the network."
      },
      AasField {
        name = "Allocation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Allocation/1/0",
        type = refBy(FixedStation_2),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Information about the time and status of application."
      }
    }
  };

  AasSubmodelElementCollectionType Applicant = {
    name = "Applicant",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Applicant/1/0",
    description = "A set of parameters that include the information about the license holder as well as the contact person.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "CompanyName",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/CompanyName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Official name of the applying/responsible company."
      },
      AasField {
        name = "ContactSurname",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/ContactSurname/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name of the responsible person."
      },
      AasField {
        name = "ContactFirstName",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/ContactFirstName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "First name of the responsible person."
      },
      AasField {
        name = "Address",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Address/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Official company address."
      },
      AasField {
        name = "Phone",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Phone/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Phone number."
      },
      AasField {
        name = "Fax",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Fax/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Fax number (optional parameter)."
      },
      AasField {
        name = "Mail",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Mail/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Contact mail address (optional parameter)."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Description/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Description of the intended use (Aproximately one page)."
      },
      AasField {
        name = "LicenseType",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/LicenseType/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Type of license: Trial operation, normal operation."
      }
    }
  };

  AasSubmodelElementCollectionType Frequency = {
    name = "Frequency",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Frequency/1/0",
    description = "This list contains information about the used frequency or frequency ranges. Only one of the three fields must be filled in. Only one of the three fields must be filled in.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "Frequency",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Frequency/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Frequency for operation in the radio spectrum, subject to license application."
      },
      AasField {
        name = "Frequencies",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Frequencies/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "List of frequencies for operation in the radio spectrum, subject to license application."
      },
      AasField {
        name = "FrequencyRange",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FrequencyRange/1/0",
        type = refBy(AasRangeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "List with “start frequency” and “stop frequency” for operation in the radio spectrum, subject to license application."
      }
    }
  };

  AasSubmodelElementCollectionType Operation = {
    name = "Operation",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Operation/1/0",
    description = "This list contains information on the period of use of the frequency ranges applied for.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "Day",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Operation/Day/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Begin of operation Day."
      },
      AasField {
        name = "Month",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Operation/Month/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Begin of operation Month."
      },
      AasField {
        name = "Year",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Operation/Year/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Begin of operation Year."
      },
      AasField {
        name = "Duration",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Operation/Duration/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Duration of operation in Days."
      }
    }
  };

  AasSubmodelElementCollectionType FixedStation = {
    name = "FixedStation",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License//1/0",
    description = "It contains the geographical information of the fixed station. Information must include both the postal and geographic address, as well as the height of the antennas.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "Location",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/Location/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Address of the location."
      },
      AasField {
        name = "GeographicPosition",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/GeographicPosition/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Geographic Position in East and North Degree, Minute and Second."
      },
      AasField {
        name = "AltituteLocation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/AltituteLocation/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Altitide above sea level."
      },
      AasField {
        name = "AltituteAntenna",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/AltituteAntenna/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Altitude above earth's surface."
      },
      AasField {
        name = "StationID",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/StationID/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identfication number of fixed stations."
      }
    }
  };

  AasSubmodelElementCollectionType FixedStation_2 = {
    name = "FixedStation_2",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Allocation/1/0",
    description = "Information about the time and status of application.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "TimeofRequest",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Allocation/TimeofRequest/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Date of application submission."
      },
      AasField {
        name = "TimeOfResponse",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Allocation/TimeOfResponse/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Date of response from regulatory authority."
      },
      AasField {
        name = "Status",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/Allocation/Status/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Response from the regulatory authority. Application approved/Application approved subject to compliance with conditions/Application rejected."
      },
      AasField {
        name = "Conditions",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/FixedStation/AltituteAntenna/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Conditions so that the license application is accepted. e.g., restriction of transmission power, alignment of the antenna."
      }
    }
  };

  AasSubmodelElementCollectionType MobileStation = {
    name = "MobileStation",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/MobileStation/1/0",
    description = "This list contains the information about the mobile station (used geographical area, number of mobile stations and kind of operation).",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "Region",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/MobileStation/Region/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Region in which the mobile should be used. Specification of the coverage area. If possible, a description using a Google Maps image with the BS and coverage area shown as a circle or polygon."
      },
      AasField {
        name = "StationID",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/MobileStation/StationID/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Id of the mobile station."
      },
      AasField {
        name = "KindOfOperation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/License/MobileStation/KindOfOperation/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "One way or two-way communication."
      }
    }
  };

  AasSubmodelElementCollectionType LicenseAcquisitionTechnicalData = {
    name = "LicenseAcquisitionTechnicalData",
    semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/1/0",
    description = "This list contains the technical parameters of the network.",
    versionIdentifier = "IDTA 02022-1-0",
    fields = {
      AasField {
        name = "EquipmentMarketing",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/EquipmentMarketing/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name or code of the equipment. This parameter is optional for a “Trial License”."
      },
      AasField {
        name = "Manufacturer",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/Manufacturer/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Manufacturer of the equipment. This parameter is optional for a “Trial License”."
      },
      AasField {
        name = "DuplexMethod",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/DuplexMethod/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"FDD, TDD"},
        description = "Duplex mode (FDD or TDD)."
      },
      AasField {
        name = "AccessMethod",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/AccessMethod/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Access mode, e.g., FDMA."
      },
      AasField {
        name = "TypeOfModulation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/TypeOfModulation/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Type of modulation, e.g., FM."
      },
      AasField {
        name = "Bandwidth",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/Bandwidth/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Used bandwith in MHz."
      },
      AasField {
        name = "TransmitterOutputPower",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/TransmitterOutputPower/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Transmitter output power in dBm, e.g., 10dBm."
      },
      AasField {
        name = "AntennaGain",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/AntennaGain/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Antenna gain in dBi respectively antenna pattern."
      },
      AasField {
        name = "Polarisation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/Polarisation/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Polarisation of the antenna, e.g., horizontal, vertical, …."
      },
      AasField {
        name = "Azimuth",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/Azimuth/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Azimut of the antenna."
      },
      AasField {
        name = "Elevation",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/Elevation/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Elevation of the antenna."
      },
      AasField {
        name = "Interfaces",
        semanticId = "iri:https://admin-shell.io/idta/WirelessCommunication/LicenseAcquisitionTechnicalData/Interfaces/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Connection between radio system and other telecommunication systems, e.g., private or public network. This parameter is optional for a “Trial License”."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
