# oktoflow platform: Data formats

This file is supposed to provide a catalogue of various fixed data formats, e.g., setup, descriptors, that the oktoflow platform is employing. While the individual formats shall be discussed in/through the README.md of the individual components, this catalog shall provide an easy access through listing the names of the respective formats and giving a hyperlink to the documentation.

## Descriptors

- Container deployment descriptor [Docker](../resources/ecsRuntime.docker/README.md) and (in preparation) [LXC](../resources/ecsRuntime.lxc/README.md)
- Service deployment descriptor for [Spring](../services/services.spring/README.md)

## Setup files

- [Management UI](../managementUI/README.md)
- [Central platform services](../platform/README.md) with [configuration.easy](../configuration/configuration.easy/README.md) and possibly [monitoring](../resources/monitoring.prometheus/README.md)
- Monitoring with [Prometheus](../resources/monitoring.prometheus/README.md)
- ECS runtime for [Docker](../resources/ecsRuntime.docker/README.md) and (in preparation) [LXC](../resources/ecsRuntime.lxc/README.md)
- Service manager for [Spring](../services/services.spring/README.md)
- Device/container software installation paths, aka [InstalledDependencies](../support/support/README.md)
- Local [YAML-based Identity store](../support/support.aas/README.md)
- Shortcut [device nameplate](../support/support.iip-aas/README.md)
- Distributed testing and evaluation environment

## Semantic Id resolution catalogues

- Local [YAML-based Semantic Id catalogue format](../support/support.iip-aas/README.md)

## Configuration AAS

- [Configuration AAS](../configuration/configuration.easy/README.md)

## Dashboard mapping

Currently documented here, may move when integrated into build process.

### Semantic Id value unit mapping file

`src/main/resources/semanticIdDashboard.yml` in `configuration.easy` with format

```
  tool-key:
    semantic-id1: tool-value-unit
    semantic-id2: tool-value-unit
```

and default mapping for `grafana`.

### Dashboard AAS JSON

Produced by `IvmlDashboardMapper` in `configuration.easy`, actual JSON format depends on used AAS plugin, e.g., BaSyx1 for metamodel v2 or BaSyx2 for metamodel v3. SM means SubModel, SMEC means SubModelElementCollection, SMEL means SubModelElementList, * indicates a potentially unlimited repetition of the element, ? indicates an optional element.
JSON is taken up by [oktoflow2grafana](https://github.com/iip-ecosphere/oktoflow2grafana).

- SM `dashboardSpec`
    - Property `oktoVersion`, Type `STRING`, oktoflow version
    - Property `name`, Type `STRING`, name of oktoflow app
    - Property `id`, Type `STRING`, id of oktoflow app
    - Property `version`, Type `STRING`, version of oktoflow app
    - Property `aasMetamodelVersion`, Type `STRING`, version of AAS metamodel used, value: `v2` | `v3`, determines how JSON structure looks like
    - SMEC `Rows`; dashboard rows, spec may go without rows then a default row shall be assumed
      - SMEC* _row-id_
         - Property `id`, Type `STRING`, unique id of display row
         - Property `name`, Type `STRING`, name of display row
         - Property `displayName`, Type `STRING`, optional display name, may be empty; if empty, use `name` instead
    - SMEC `Dashboard`; top-level dashboard description
         - Property `title`, Type `STRING`, title of the dashboard
         - Property `uid`, Type `STRING`, unique id of the dashboard
         - SMEC `tags`; currently empty, may be `time_from`, `time_to`, `timezone`
    - SMEC `panels`; individual single/multi-valued dashboard panels
       - SMEL _panel-id_
         - SMEC `custom-options`
           - Property? `imageUrl`, Type `STRING`, URL to logo, either `imageUrl` or `image` shall be given if panel type is `image`; may also be the name of a classpath-loadable figure that is transformed into a base64 encoded prefixed with "data:_transfer-encoding_;base64,"
           - Property `fit`, Type `STRING`, **unclear**, values `contain`
         - Property `title`, Type `STRING`, title of the panel
         - Property `unit`, Type `STRING`, value unit of the data in the panel, see grafana value units and Semantic Id value unit mapping above
         - Property `datasource_uid`, Type `STRING`, refers one of the entries in `db` below, may be empty for none, e.g., for an image panel
         - Property `bucket`, Type `STRING`, Influx bucket in `datasource_uid` specifying data source, may be empty for none, e.g., for an image panel
         - Property `measurement`, Type `STRING`, Influx measurement in `datasource_uid` specifying data source, may be empty for none, e.g., for an image panel
         - SMEC `fields`
           - SMEC* _field-name_
               - Property `name`, Type `STRING`, field from Influx measurement to display
               - Property `displayName`, Type `STRING`, display name for field, may be empty; if empty, use field `name` instead
         - Property `panel_type`, Type `STRING`, Type of the panel, one of `timeseries`, `gauge`, `barchart`, `table`, `stat`, `piechart`, `image`
         - Property `description`, Type `STRING`, description of the panel, may be empty
         - Property `displayName`, Type `STRING`, display name of the panel, may be empty; if empty, use panel `name` instead
         - Property? `row`, Type `STRING`, ref to unique display row id, if given and valid assign panel to specified row

         May be complemented with `axis_min_soft`, `axis_max_soft`, `axis_label`, legend and panel position.
    - SMEC `db`; database, data source
      - SMEC* _db-id_
          - Property? `urlPath`, Type `STRING`, optional URL path to database
          - Property `organization`, Type `STRING`, InfluxDb organization
          - Property? `token`, Type `STRING`, optional InfluxDb access token, if permitted to export from identity store

