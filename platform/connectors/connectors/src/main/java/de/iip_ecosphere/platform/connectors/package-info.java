/**
 * The connectors component defining the interface to generic machine/platform connectors. Main classes here are the 
 * <ul>
 *  <li>{@link de.iip_ecosphere.platform.connectors.Connector connector interface}</li>
 *  <li>{@link de.iip_ecosphere.platform.connectors.MachineConnector connector annotation} declaring the 
 *    capabilities of the connector, e.g., to be dynamically taken up by the code generation</li>
 *  <li>{@link de.iip_ecosphere.platform.connectors.ConnectorParameter connector parameters} when connecting to a 
 *    server/broker (including some security information like 
 *    {@link de.iip_ecosphere.platform.connectors.IdentityToken}).
 * </ul>
 * 
 * Some protocols like OPC or Asset Administration Shells (AAS) rely on an information model. Accessing this model in 
 * a uniform manner (although some functionality may not be supported depending on the protocol) is described in 
 * {@link de.iip_ecosphere.platform.connectors.model.ModelAccess}. For other protocols like MQTT this information model
 * is optional.
 * 
 * Connectors have several template parameters, including the data types accessible from the platform ({@code CI} for 
 * input into the connector, {@code CO} for output produced by the connector), the internal data types to be handed over
 * to the underlying protocol implementation ({@code I} for input into the protocol, {@code O} for output from the 
 * protocol) and {@code D} for the value data type used in the optional 
 * {@link de.iip_ecosphere.platform.connectors.model.ModelAccess model}. For payload-based protocols like MQTT, 
 * {@code I} and {@code O} define the payload type, usually {@code byte[]} while {@code CI} and {@code CO} are left open
 * to be handled by plugins based on {@link de.iip_ecosphere.platform.transport.serialization.InputTypeTranslator} and
 * {@link de.iip_ecosphere.platform.transport.serialization.OutputTypeTranslator}. For model-based protocols like OPC,
 * {@code I} and {@code O} are of less significance as the attached type translators directly query the model and 
 * produce from that instances of {@code CO} or read instances of {@code CI}. However, more information must be set
 * up there, so the type translators are refined into 
 * {@link de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeTranslator} and 
 * {@link de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator}.
 * 
 * To ease working with the type translations, a connector may have a 
 * {@link de.iip_ecosphere.platform.connectors.types.ProtocolAdapter}, its basic implementation 
 * {@link de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter} utilizes the refined type 
 * transformators. Channel-based protocols like MQTT require even more information, here provided through 
 * {@link de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter}. 
 * 
 * Moreover, basic implementations for {@link de.iip_ecosphere.platform.connectors.AbstractConnector connectors},
 * {@link de.iip_ecosphere.platform.connectors.model.AbstractModelAccess model access} or even the type translators
 * is desirable, as required conventions are already implemented. Thus, when setting up a new connector, please take
 * these classes into account. 
 * 
 * In some situations it would be desirable to reuse existing 
 * {@link de.iip_ecosphere.platform.transport.serialization.Serializer serializers}. Therefore, we offer the type 
 * translation adapters {@link de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter} and
 * {@link de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter}. They can optionally be used as 
 * input arguments while connector creation. 
 */
package de.iip_ecosphere.platform.connectors;