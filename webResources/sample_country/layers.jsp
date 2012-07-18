<%@ page session="true"
%><%@taglib uri="http://www.springframework.org/tags" prefix="spring"
%><%@ page contentType="application/json" pageEncoding="UTF-8"
%>{
  "layers": {
    "blueMarble": {
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:world_topo_bathy",
      "imageFormat": "image/jpeg",
      "visible": true
    },
    "forestClassification": {
      "label": "<spring:message code="facet_forest_classification" />",
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:forest_classification",
      "imageFormat": "image/jpeg",
      "visible": true,
      "legend": "facet_forest_classification.png",
      "sourceLink": "http://osfac.net/facet.html",
      "sourceLabel": "FACET"
    },
    "uclForestClassification": {
      "label": "<spring:message code="ucl_forest_classification" />",
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:ucl_rdc_classification",
      "imageFormat": "image/jpeg",
      "visible": true,
      "legend": "ucl_forest_classification.png",
      "sourceLink": "http://sites.uclouvain.be/enge/map_server/UCL_RDC_classification.color.tif",
      "sourceLabel": "UCL"
    },
    "landsat": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsTime":  "2000-01-01T00:00:00.000Z,2005-01-01T00:00:00.000Z",
      "wmsName": "unredd:landsat-time",
      "imageFormat": "image/jpeg",
      "visible": true,
      "sourceLink": "http://osfac.net/facet.html",
      "sourceLabel": "FACET"
    },
    "hillshade": {
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:color_hillshade",
      "imageFormat": "image/jpeg",
      "visible": true,
      "sourceLink": "http://srtm.csi.cgiar.org/",
      "sourceLabel": "CGIARS"
    },
    "deforestation": {
      "label": "<spring:message code="deforestation" />",
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:deforestation_2000-2010",
      "imageFormat": "image/png8",
      "visible": true,
      "legend": "deforestation.png",
      "sourceLink": "http://osfac.net/facet.html",
      "sourceLabel": "FACET"
    },
    "trainingData": {
      "label": "<spring:message code="preliminary_material" />",
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "nov_training_2011_data",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "nov_workshop_data.png"
    },
    "reddPlusProjects": {
      "label": "<spring:message code="redd_plus_projects" />",
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:redd_plus_projects",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "redd_plus_projects.png",
      "sourceLink": "http://www.observatoire-comifac.net/",
      "sourceLabel": "OFAC"
    },
    "reddPlusProjects_simp": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:redd_plus_projects_simp",
      "imageFormat": "image/png",
      "visible": false,
      "queryable": true
    },
    "reddPlusInitiatives": {
      "label": "<spring:message code="redd_plus_initiatives" />",
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:redd_plus_initiatives",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "redd_plus_initiatives.png",
      "sourceLink": "http://www.observatoire-comifac.net/",
      "sourceLabel": "OFAC"
    },
    "reddPlusInitiatives_simp": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:redd_plus_initiatives_simp",
      "imageFormat": "image/png",
      "visible": false,
      "queryable": true
    },
    "hydrography": {
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "hydrography",
      "imageFormat": "image/png",
      "visible": true,
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "intactForest": {
      "label": "<spring:message code="intact_forest" />",
      "baseUrl": "/geoserver_drc/wms",
      "wmsTime": "2000-01-01T00:00:00.000Z,2005-01-01T00:00:00.000Z,2010-01-01T00:00:00.000Z",
      "wmsName": "unredd:greenpeace_ifl",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "intact_forest.png",
      "sourceLink": "http://www.intactforests.org/",
      "sourceLabel": "www.intactforests.org"
    },
    "loggingConcessions": {
      "label": "<spring:message code="logging_concessions" />",
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:logging_concessions",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "logging_concessions.png",
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "loggingConcessions_simp": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:logging_concessions_simp",
      "imageFormat": "image/jpeg",
      "visible": false,
      "queryable": true
    },
    "protectedAreas": {
      "label": "<spring:message code="protected_areas" />",
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:protected_areas",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "protected_areas.png",
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "protectedAreas_simp": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:protected_areas_simp",
      "imageFormat": "image/png",
      "visible": false,
      "queryable": true
    },
    "administrativeUnits": {
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:admin_units",
      "imageFormat": "image/png",
      "visible": true,
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "administrativeUnits_simp": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:admin_units_simp",
      "imageFormat": "image/png",
      "visible": false,
      "queryable": true
    },
    "provinces": {
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "provinces_labels",
      "imageFormat": "image/png",
      "visible": true,
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "provinces_simp": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:provinces",
      "imageFormat": "image/png",
      "visible": false,
      "queryable": true
    },
    "roads": {
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:roads",
      "imageFormat": "image/png",
      "visible": true,
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "settlements": {
      "label": "<spring:message code="settlements" />",
      "baseUrl": "/geoserver_drc/wms",
      "wmsName": "unredd:settlements",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "settlements.png",
      "sourceLink": "http://www.wri.org/publication/interactive-forest-atlas-democratic-republic-of-congo",
      "sourceLabel": "WRI"
    },
    "ecoregions": {
      "label": "<spring:message code="ecoregions" />",
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:ecoregions",
      "imageFormat": "image/png",
      "visible": true,
      "legend": "ecoregions.png",
      "sourceLink": "http://www.worldwildlife.org/science/ecoregions/item1267.html)",
      "sourceLabel": "WWF"
    }
  },
  
  "contexts": {
    "blueMarble": {
      "active": true,
      "infoFile": "bluemarble_def.html",
      "label": "<spring:message code="blue_marble" />",
      "layers": ["blueMarble"]
    },
    "facetForestClassification": {
      "infoFile": "forest_classification_def.html",
      "label": "<spring:message code="facet_forest_classification" />",
      "layers": ["forestClassification"]
    },
    "uclForestClassification": {
      "infoFile": "ucl_forest_classification_def.html",
      "label": "<spring:message code="ucl_forest_classification" />",
      "layers": ["uclForestClassification"]
    },
    "landsat": {
      "infoFile": "landsat_def.html",
      "label": "<spring:message code="landsat" />",
      "layers": ["landsat"]
    },
    "hillshade": {
      "infoFile": "hillshade_def.html",
      "label": "<spring:message code="hillshade" />",
      "layers": ["hillshade"]
    },
    "deforestation": {
      "infoFile": "deforestation_def.html",
      "label": "<spring:message code="deforestation" />",
      "layers": ["deforestation"]
    },
    "trainingData": {
      "infoFile": "training_data_def.html",
      "label": "<spring:message code="preliminary_material" />",
      "layers": ["trainingData"]
    },
    "reddPlusProjects": {
      "infoFile": "redd_plus_projects_def.html",
      "label": "<spring:message code="redd_plus_projects" />",
      "layers": ["reddPlusProjects", "reddPlusProjects_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:redd_plus_projects&TRANSPARENT=true"
    },
    "reddPlusInitiatives": {
      "infoFile": "redd_plus_initiatives_def.html",
      "label": "<spring:message code="redd_plus_initiatives" />",
      "layers": ["reddPlusInitiatives", "reddPlusInitiatives_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:redd_plus_projects&STYLE=redd_plus_initiatives&TRANSPARENT=true"
    },
    "hydrography": {
      "infoFile": "hydrography_def.html",
      "label": "<spring:message code="hydrography" />",
      "layers": ["hydrography"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:hydro_surface&TRANSPARENT=true"
    },
    "loggingConcessions": {
      "infoFile": "logging_concessions_def.html",
      "label": "<spring:message code="logging_concessions" />",
      "layers": ["loggingConcessions", "loggingConcessions_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:logging_concessions&TRANSPARENT=true"
    },
    "protectedAreas": {
      "infoFile": "protected_areas_def.html",
      "label": "<spring:message code="protected_areas" />",
      "layers": ["protectedAreas", "protectedAreas_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:protected_areas&TRANSPARENT=true"
    },
    "provinces": {
      "active": true,
      "infoFile": "provinces_def.html",
      "label": "<spring:message code="provinces" />",
      "layers": ["provinces", "provinces_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:provinces&TRANSPARENT=true"
    },
    "administrativeUnits": {
      "infoFile": "administrative_boundaries_def.html",
      "label": "<spring:message code="admin_units" />",
      "layers": ["administrativeUnits", "administrativeUnits_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:admin_units&TRANSPARENT=true"
    },
    "roads": {
      "infoFile": "roads_def.html",
      "label": "<spring:message code="roads" />",
      "layers": ["roads"]
    },
    "settlements": {
      "infoFile": "settlements_def.html",
      "label": "<spring:message code="settlements" />",
      "layers": ["settlements"]
    },
    "intactForest": {
      "infoFile": "intact_forest_def.html",
      "label": "<spring:message code="intact_forest" />",
      "layers": ["intactForest"]
    },
    "ecoregions": {
      "infoFile": "ecoregions_def.html",
      "label": "<spring:message code="ecoregions" />",
      "layers": ["ecoregions"]
    },
    "reddPlusActivitiesDeforestation": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="deforestation" />"
    },
    "reddPlusActivitiesDegradation": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="degradation" />"
    },
    "reddPlusActivitiesEnhancement": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="enhancement" />"
    },
    "reddPlusActivitiesConservation": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="conservation" />"
    },
    "reddPlusActivitiesSustainableForestManagement": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="sustainable_management" />"
    },
    "environmental": {
      "infoFile": "environmental.html",
      "label": "<spring:message code="environmental" />"
    },
    "social": {
      "infoFile": "social.html",
      "label": "<spring:message code="social" />"
    },
    "degradation": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="degradation" />"
    },
    "regrowth": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="regrowth" />"
    },
    "conservation": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="conservation" />"
    },
    "afforestation": {
      "infoFile": "afforestation_def.html",
      "label": "<spring:message code="afforestation" />"
    },
    "reforestation": {
      "infoFile": "reforestation_def.html",
      "label": "<spring:message code="reforestation" />"
    },
    "activeFire": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="active_fire" />"
    },
    "burntArea": {
      "infoFile": "data_not_available.html",
      "label": "<spring:message code="burnt_area" />"
    }
  },
  
  "contextGroups": 
  {
    "items": [
      {
        "group": {
          "label": "<spring:message code="base_layers" />",
          "items": [
            { "context": "blueMarble" },
            { "context": "facetForestClassification" },
            { "context": "uclForestClassification" },
            { "context": "landsat" },
            { "context": "hillshade" }
          ]
        }
      },
      {
        "group": {
          "label": "<spring:message code="admin_areas" />",
          "items": [
            { "context": "provinces" },
            { "context": "administrativeUnits" }
          ]
        }
      },
      {
        "group": {
          "label": "<spring:message code="redd_plus_activity" />",
          "infoFile" : "redd_plus_activities_def.html",
          "items": [
            { "context": "reddPlusActivitiesDeforestation" },
            { "context": "reddPlusActivitiesDegradation" },
            { "context": "reddPlusActivitiesEnhancement" },
            { "context": "reddPlusActivitiesConservation" },
            { "context": "reddPlusActivitiesSustainableForestManagement" }
          ]
        }
      },
      {
        "group": {
          "label": "<spring:message code="redd_plus_registry" />",
          "items": [
            { "context": "reddPlusProjects" },
            { "context": "reddPlusInitiatives" }
          ]
        }
      },
      {
        "group": {
          "label": "<spring:message code="forest_area_and_forest_area_change" />",
          "infoFile" : "forest_area_and_forest_area_changes_def.html",
          "items": [
            {
              "group": {
                "label": "<spring:message code="forest_land_remaining_forest_land" />",
                "items": [
                  { "context": "degradation" },
                  { "context": "regrowth" },
                  { "context": "conservation" }
                ]
              }
            },
            {
              "group": {
                "label": "<spring:message code="forest_land_converted_to_non_forest_land" />",
                "items": [
                  { "context": "deforestation" },
                  { "context": "trainingData" },
                  { "context": "intactForest" }
                ]
              }
            },
            {
              "group": {
                "label": "<spring:message code="non_forest_land_converted_to_forest_land" />",
                "items": [
                  { "context": "afforestation" },
                  { "context": "reforestation" }
                ]
              }
            },
            {
              "group": {
                "label": "<spring:message code="biomass_burining" />",
                "items": [
                  { "context": "activeFire" },
                  { "context": "burntArea" }
                ]
              }
            }
          ]
        }
      },
      {
        "group": {
          "label": "<spring:message code="safeguards" />",
          "items": [
            { "context": "environmental" },
            { "context": "social" }
          ]
        }
      },
      {
        "group": {
          "label": "<spring:message code="other" />",
          "items": [
            { "context": "protectedAreas" },
            { "context": "loggingConcessions" },
            { "context": "hydrography" },
            { "context": "ecoregions" },
            { "context": "roads" },
            { "context": "settlements" }
          ]
        }
      }
    ]
  },
  
  <%-- Add localized strings used to dynamically create content in Javascript.
  Please note: this is a temporary solution before the json definition for statistics
  will be created from the GeoStore data --%>
  
  "lang": {
    "province": "<spring:message code="province" />",
    "deforestation_drivers": "<spring:message code="deforestation_drivers" />",
    "admin_unit": "<spring:message code="admin_unit" />",
    "logging": "<spring:message code="logging" />",
    "protected_area": "<spring:message code="protected_area" />",
    "redd_plus_activity": "<spring:message code="redd_plus_activity" />"
  }
}
