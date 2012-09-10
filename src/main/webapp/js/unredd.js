/*
 * nfms4redd Portal Interface - http://nfms4redd.org/
 *
 * (C) 2012, FAO Forestry Department (http://www.fao.org/forestry/)
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

OpenLayers.ProxyHost = "proxy?url=";
OpenLayers.ImgPath = "images/openlayers/";

var RecaptchaOptions = {
	lang: languageCode,
	theme: 'blackglass'
};

var UNREDD = {
    allLayers: {},
    visibleLayers: [],
    queryableLayers: [],
    timeDependentLayers: [],
    mapContexts: {},
    fb_toolbar: {},
    times: []
};

UNREDD.Layer = function (layerId, layerDefinition)
{
    /*
    layerDefinition object example:

    "forestClassification": {
      "id": "layer_id",
      "label": "<spring:message code="facet_forest_classification" />",
      "baseUrl": "/geoserver_drc/gwc/service/wms",
      "wmsName": "unredd:forest_classification",
      "imageFormat": "image/png8",
      "visible": true,
      "legend": "facet_forest_classification.png",
      "sourceLink": "http://osfac.net/facet.html",
      "sourceLabel": "FACET",
      "queryable": true
    }
    */
	this.name = layerId
    this.configuration = layerDefinition;
    
    // set WMS servers urls
    var baseUrl = layerDefinition.baseUrl;
    var urls = [];
    if ((/^http:/).test(baseUrl)) {
    	// If LayerDefinition is an absolute URL, don't use UNREDD.wmsServers
    	urls = [baseUrl];
    } else {
	    var urlsLength = UNREDD.wmsServers.length;
	    for (var i = 0; i < urlsLength; i++) {
	        var server = UNREDD.wmsServers[i];
	        urls.push(server + baseUrl);
	    }
    }
    
    // Set WMS paramaters that are common to all layers
    var wmsParams = {layers: layerDefinition.wmsName, format: layerDefinition.imageFormat, transparent: true};

    // Add custom wms parameters
    var wmsParameters = layerDefinition.wmsParameters;
    for (var paramName in wmsParameters) {
        if (wmsParameters.hasOwnProperty(paramName)) {
            wmsParams[paramName] = wmsParameters[paramName];
        }
    }

    // Create the OpenLayers object for this layer
    this.olLayer = new OpenLayers.Layer.WMS(
        layerId,
        urls,
        wmsParams,
        {transitionEffect: "resize", removeBackBufferDelay: 0, isBaseLayer: false, 'buffer': 0, visibility: layerDefinition.visible === 'true', projection: 'EPSG:900913', noMagic: true}
    );
}


UNREDD.Context = function (contextId, contextDefinition)
{
    /*
    contextDefinition object example:

    "administrativeUnits":{
      "id": "context_id",
      "infoFile": "administrative_boundaries_def.html",
      "label": "<spring:message code="admin_units" />",
      "layers": ["administrativeUnits", "administrativeUnits_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:admin_units&TRANSPARENT=true"
    }
    */

    var nLayers = 0;

    this.name = contextId;
    this.configuration = contextDefinition;
    this.layers = [];

    this.setVisibility = function (active) {
        for (var i = 0; i < nLayers; i++) {
            this.layers[i].olLayer.setVisibility(active);
            this.configuration.active = active;
        }
    }

    if (contextDefinition.layers) {
        nLayers = contextDefinition.layers.length;
        for (var i = 0; i < nLayers; i++) {
            var layerName = contextDefinition.layers[i];
            this.layers.push(UNREDD.allLayers[layerName]);
       }
    }

    this.hasLegend = (function() {
        for (var i = 0; i < nLayers; i++) {
            if (this.layers[i].configuration.hasOwnProperty('legend')) return true;
        }
        
        return false;
    }).call(this);
}

var isoDateString;

Date.prototype.setISO8601 = function (string) {
    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
            "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\\.([0-9]+))?)?" +
            "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?",
        d = string.match(new RegExp(regexp));
    if (d) {
        var date = new Date(d[1], 0, 1),
        offset = 0,
        time;
	
	    if (d[3])  {date.setMonth(d[3] - 1);}
	    if (d[5])  {date.setDate(d[5]);}
	    if (d[7])  {date.setHours(d[7]);}
	    if (d[8])  {date.setMinutes(d[8]);}
	    if (d[10]) {date.setSeconds(d[10]);}
	    if (d[12]) {date.setMilliseconds(Number("0." + d[12]) * 1000);}
	    if (d[14]) {
	        offset = (Number(d[16]) * 60) + Number(d[17]);
	        offset *= ((d[15] === '-') ? 1 : -1);
	    }
	
	    offset -= date.getTimezoneOffset();
	    time = (Number(date) + (offset * 60 * 1000));
	
	    this.setTime(Number(time));
	    return true;
    } else {
    	return false;
    }
};

isoDateString = function (d) {
    // 2000-01-01T00:00:00.000Z
    function pad(n) {
        return n < 10 ? '0' + n : n;
    }
    return d.getUTCFullYear() + '-'
        + pad(d.getUTCMonth() + 1) + '-'
        + pad(d.getUTCDate()) + 'T'
        + pad(d.getUTCHours()) + ':'
        + pad(d.getUTCMinutes()) + ':'
        + pad(d.getUTCSeconds()) + '.'
        + pad(d.getUTCMilliseconds()) + 'Z';
};

$(document).ready(function () {
    // disable text selection on Explorer (done with CSS in other browsers)
    document.body.onselectstart = function() { return false; };
});

$(window).load(function () {
    var openLayersOptions,
        styleMap, // TODO: check if the following ones can be local to some function
        highlightLayer,
        showInfo,
        setLayersTime,
        selectedDate,
        click,
        legendOn = false,
        year,
        //markers,
        infoControl,
        getClosestPastDate,
        getClosestFutureDate,
        updateActiveLayersPane,
        mapContexts = {},
        setContextVisibility,
        resizeMapDiv;

    // Set map div height
    resizeMapDiv = function () {
        var bannerHeight = $('#header').height();

        $('#map').css('top', bannerHeight);
        $('#map').css('height', $(window).height() - bannerHeight);
        $('#map').css('width', $(window).width());
    }
    
    $(window).resize(function() {
        resizeMapDiv();
    });

    resizeMapDiv();
    
    setContextVisibility = function (context, active) {
        context.setVisibility(active);

        var icon = $('#' + context.name + '_inline_legend_icon');
        if (active) {
            icon.addClass('on');
            icon.click(function (event) {
                openLegend($('#' + context.name + '_legend'));
                event.stopPropagation();
                return false;
            });
        } else {
            icon.removeClass('on');
            icon.off('click');
            icon.click(function (event) {
                event.stopPropagation();
                return false;
            });
        }
    };
    
    openLayersOptions = {
        theme:             null,
        projection:        new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        units:             "m",
        maxResolution:     UNREDD.maxResolution,
        maxExtent:         UNREDD.maxExtent,
        restrictedExtent:  UNREDD.restrictedExtent,
        allOverlays:       true,
        controls:          [
                               new OpenLayers.Control.Navigation({ documentDrag: true, zoomWheelEnabled: false }),
                               new OpenLayers.Control.Scale()
                           ]
        //numZoomLevels:   18
    };

    UNREDD.map = new OpenLayers.Map('map', openLayersOptions);


    $.ajax({
        url: 'layers.json', 
        dataType: 'json', 
        async: false, 
        success: function (data_) {
            var setupAllContexts,
            setLegends,
            loadContextGroups;

            // Create layers objects
            jQuery.each(data_.layers, function (i, layerDefinition) {
                var layerId = layerDefinition.id;
                var layer = new UNREDD.Layer(layerId, layerDefinition);

                if (layerDefinition.visible) {
                    UNREDD.visibleLayers.push(layer.olLayer);
                }

                UNREDD.allLayers[layerId] = layer;
                if (layerDefinition.queryable) {
                    UNREDD.queryableLayers.push(layer.olLayer);
                }
                if (typeof layer.configuration.wmsTime !== 'undefined') {
                    UNREDD.timeDependentLayers.push(layer)
                }
            });

            // Create context objects
            jQuery.each(data_.contexts, function (i, contextDefinition) {
                var contextId = contextDefinition.id;
                var context = new UNREDD.Context(contextId, contextDefinition);
                UNREDD.mapContexts[contextId] = context;
            });
        
            var contextGroups = data_.contextGroups;

            setupAllContexts = function () {
                // look for active contexts
                jQuery.each(UNREDD.mapContexts, function (contextName, context) {
                    var active = typeof context.configuration.active !== 'undefined' && context.configuration.active;
                    setContextVisibility(context, active);
                });
            };

            updateActiveLayersPane = function () {
                var //div,
                table, tr, td, td2, layers, inlineLegend, transparencyDiv;
                // empty the active_layers div (layer on the UI -> context here)
                $('#active_layers_pane div').empty();

                table = $('<table style="width:90%;margin:auto"></table>');
                $('#active_layers_pane div').append(table);

                jQuery.each(UNREDD.mapContexts, function (contextName, context) {
                    var contextConf = context.configuration;
                
                    if (contextConf.active) {
                        // First row: inline legend and context name
                        tr = $('<tr></tr>');

                        if (contextConf.hasOwnProperty('inlineLegendUrl')) {
                            td = $('<td style="width:20px"></td>');
                            inlineLegend = $('<img class="inline-legend" src="' + UNREDD.wmsServers[0] + contextConf.inlineLegendUrl + '">');
                            td.append(inlineLegend);
                            tr.append(td);
                            td2 = $('<td></td>');
                        } else {
                            td2 = $('<td colspan="2"></td>');
                        }
                        td2.append(contextConf.label);
                        tr.append(td2);
                        table.append(tr);

                        // Another row
                        tr = $('<tr></tr>');
                        transparencyDiv = $('<div style="margin-top:4px; margin-bottom:12px;" id="' + contextName + '_transparency_slider"></div>');
                        td = $('<td colspan="2"></td>');
                        td.append(transparencyDiv);
                        tr.append(td);
                        table.append(tr);

                        layers = contextConf.layers;

                        (function (contextLayers) {
                            $(transparencyDiv).slider({
                                min: 0,
                                max: 100,
                                value: 100,
                                slide: function (event, ui) {
                                    $.each(contextLayers, function (n, layer) {
                                        layer.olLayer.setOpacity(ui.value / 100);
                                    });
                                }
                            });
                        }(context.layers));
                    }
                });
            };

            // Add the legend images to the legend pane.
            // This implementation works only if two contexts don't have a layer in common.
            // A better implementation would have to scan all the active contexts and see which layers should be visible
            setLegends = function (context, contextIsActive) {
                jQuery.each(context.layers, function (n, layer) {
                    var //legendFile,
                    layerConf = layer.configuration,
                    table,
                    legendName;

                    if (layerConf.visible && typeof layerConf.legend !== "undefined") {
                        //legendFile = layerDef.legend;
                        legendName = context.name + '_legend';

                        if (!contextIsActive) {
                            $('#' + legendName).remove();
                        } else {
                            table  = '<table class="layer_legend" id="' + legendName + '">';
                            table += '<tr class="legend_header">';
                            table += '<td class="layer_name">' + layerConf.label + '</td>';
                        
                            if (typeof layerConf.sourceLink !== "undefined") {
                                table += '<td class="data_source_link"><span class="lang" id="data_source">'+messages.data_source+':</span> <a target="_blank" href="' + layerConf.sourceLink + '">' + layerConf.sourceLabel + '</a></td>';
                            } else {
                                table += "<td></td>";
                            }
                            table += '</tr>';
                            table += '<tr class="legend_image">';
                            table += '<td colspan="2" style="width:100%;background-color:white"><img src="static/loc/' + languageCode + '/images/' + layerConf.legend + '" /></td>';
                            table += '</tr>';
                            table += '</table>';
                        }

                        $('#legend_pane_content').append(table);
                    }
                });
            };

            // Though recursive and ready for n level groupings with some adjustments, this function
            // is meant to work with three level grouping of contexts
            // TODO: use some templating engine?
            loadContextGroups = function (contextGroups, level, element) {
                jQuery.each(contextGroups.items, function (contextGroupName, contextGroupDefinition) {
                    var innerElement = null,
                    accordionHeader,
                    contextsDiv,
                    header,
                    contextName,
                    tr,
                    td3,
                    td4,
                    infoButton,
                    inlineLegend,
                    active;

                    if (contextGroupDefinition.hasOwnProperty('group')) {
                        // it's a group
                        if (level === 0) {
                            // it's an accordion header
                            if (typeof contextGroupDefinition.group.infoFile !== 'undefined') {
                                // accordion header has a info file - we add info button
                                accordionHeader = $('<div style="position:relative" class="accordion_header"><a style="width:190px" href="#">' + contextGroupDefinition.group.label
                                    + '</a></div>');
                                infoButton = $('<a style="position:absolute;top:3px;right:7px;width:16px;height:16px;padding:0;" class="layer_info_button" href="static/loc/' + languageCode + '/html/' + contextGroupDefinition.group.infoFile + '"></a>')
                                accordionHeader.append(infoButton);
                            
                                // prevent accordion item from expanding when clicking on the info button
                                infoButton.click(function (event) {
                                	event.stopPropagation();
                                });
                                
                                //if (typeof infoButton !== 'undefined') {
                                infoButton.fancybox({
                                    'autoScale' : false,
                                    'openEffect' : 'elastic',
                                    'closeEffect' : 'elastic',
                                    'type': 'ajax',
                                    'overlayOpacity': 0.5
                                });
                                //}

                            } else {
                                accordionHeader = $("<div class=\"accordion_header\"><a href=\"#\">" +  contextGroupDefinition.group.label + "</a></div>");
                            }
                            element.append(accordionHeader);
                            contextsDiv = $("<div class=\"context_buttonset\"></div>");
                            innerElement = $('<table style="width:100%;border-collapse:collapse"></table>');

                            contextsDiv.append(innerElement);
                            element.append(contextsDiv);
                        } else {
                            // we are inside of an accordion element
                            header = $("<div><a href=\"#\">" + contextGroupDefinition.group.label + "</a></div>");
                            element.append(header);
                            innerElement = $('<table class="second_level" style="width:100%"></table>');
                            element.append(innerElement);
                        }

                        loadContextGroups(contextGroupDefinition.group, level + 1, innerElement);
                    } else {
                        // it's a context in a group
                        if (element !== null) {
                            contextName = contextGroupDefinition.context;
                            active = UNREDD.mapContexts[contextName].configuration.active;

                            var context = UNREDD.mapContexts[contextName];

                            if (typeof context !== "undefined") {
                                var contextConf = context.configuration;

                                tr = $('<tr class="layer_row">');

                                //if (active) {
                                //  tr.addClass('active');
                                //}
                            
                                if (contextConf.hasOwnProperty('inlineLegendUrl')) {
                                    // context has an inline legend
                                    var td1 = $('<td style="width:20px">');
                                    inlineLegend = $('<img class="inline-legend" src="' + UNREDD.wmsServers[0] + contextConf.inlineLegendUrl + '">');
                                    td1.append(inlineLegend);
                                } else if (context.hasLegend) {
                                    // context has a legend to be shown on the legend pane - we add a link to show the legend pane
                                    if (active) {
                                        td1 = $('<td style="font-size:9px;width:20px;height:20px"><a id="' + contextName + '_inline_legend_icon" class="inline_legend_icon on"></a></td>');
                                        // add the legend to the legend pane (hidden when page loads)
                                        setLegends(context, true);
                                    } else {
                                        td1 = $('<td style="font-size:9px;width:20px;height:20px"><a id="' + contextName + '_inline_legend_icon" class="inline_legend_icon"></a></td>');
                                    }
                                } else if (typeof contextConf.layers !== "undefined") {
                                    td1 = $('<td></td>');
                                }

                                if (typeof contextConf.layers !== "undefined") {
                                    // context actually contains layers
                                    var td2 = $('<td style="width:16px"></td>');
                                    var checkbox = $('<div class="checkbox" id="' + contextName + "_checkbox" + '"></div>');
                                    if (active) {
                                        checkbox.addClass('checked');
                                    }

                                    (function (element) {
                                        // emulate native checkbox behaviour
                                        element.mousedown(function () {
                                            element.addClass('mousedown');
                                        }).mouseup(function () {
                                            element.removeClass('mousedown');
                                        }).mouseleave(function () {
                                            element.removeClass('in');
                                        }).mouseenter(function () {
                                            element.addClass('in');
                                        }).click(function () {
                                            element.toggleClass('checked');
                                        
                                            var active = !contextConf.active;
                                            setContextVisibility(context, active);
                                            setLegends(context, active);
                                            updateActiveLayersPane();
                                        });
                                    }(checkbox));

                                    td2.append(checkbox);
                                }

                                td3 = $('<td style="color:#FFF">');
                                td3.text(contextConf.label);

                                td4        = $('<td style="width:16px;padding:0">');
                                infoButton = $('<a class="layer_info_button" id="' + contextName + '_info_button" href="static/loc/' + languageCode + '/html/' + contextConf.infoFile + '"></a>');
                            
                                if (typeof contextConf.infoFile !== 'undefined') {
                                    td4.append(infoButton);
                                }

                                if (td1) {
                                    tr.append(td1);
                                }
                                if (td2) {
                                    tr.append(td2);
                                }

                                tr.append(td3, td4);

                                element.append(tr);
                            } else if (typeof contextConf !== "undefined") {
                                tr = $('<tr style="font-size:10px;height:22px">');
                                td1 = $('<td style="color:#FFF" colspan="3">');
                                td1.text(contextConf.label);
                                td2 = $('<td style="width:16px;padding:0">');
                                infoButton = $('<a class="layer_info_button" id="' + contextName + '_info_button" href="loc/' + languageCode + '/html/' + contextConf.infoFile + '"></a>');

                                td2.append(infoButton);

                                tr.append(td1, td2);
                                element.append(tr);
                            }

                            if (typeof infoButton !== 'undefined') {
                                infoButton.fancybox({
                                    'autoScale': false,
                                    'openEffect': 'elastic',
                                    'closeEffect': 'elastic',
                                    'type': 'ajax',
                                    'overlayOpacity': 0.5
                                });
                            }
                        }
                    }
                });
            };
           
            loadContextGroups(contextGroups, 0, $("#layers_pane"));

            $("#layers_pane").accordion({
                collapsible: true, 
                autoHeight: false, 
                header: ".accordion_header", 
                animated: false
            } );
            $("#layers_pane").show();
        
            setupAllContexts();


            // create info dialog
            var selectedFeatures = {};
            $("#info_popup").dialog({
                closeOnEscape: true,
                //height: 170,
                //minHeight: 400,
                //maxHeight: 800,
                width: 300,
                zIndex: 2000,
                resizable: false,
                close: function (event, ui) {
                    // destroy all features
                    $.each(selectedFeatures, function (layerId, feature) {
                        feature.destroy();
                    });
                },
                autoOpen: false
            });

            showInfo = function (evt) {
                var x = evt.xy.x - 100,
                    y = evt.xy.y - 200,
                    i,
                    feature,
                    featureType,
                    nSelectedFeatures = 0,
                    infoPopup = $("#info_popup");

                highlightLayer.destroyFeatures();
                selectedFeatures = {};

                if (evt.features && evt.features.length) {
                    var viewportExtent = UNREDD.map.getExtent();

                    // re-project to Google projection
                    for (i = 0; i < evt.features.length; i++) {
                        evt.features[i].geometry.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));

                        // don't select it if most of the polygon falls outside of the viewport
                        if (!viewportExtent.scale(1.3).containsBounds(evt.features[i].geometry.getBounds())) {
                          continue;
                        }

                        feature = evt.features[i];
                        featureType = feature.gml.featureType;
                        selectedFeatures[featureType] = feature;
                        nSelectedFeatures += 1;
                    }

                    infoPopup.empty();

                    // handle custom popup - info will be taken from json but for now it's in the custom.js. Don't have time
                    var customPopupLayer = null;
                    $.each(selectedFeatures, function (layerId, feature) {
                        info = UNREDD.layerInfo[layerId](feature);
                        if (typeof(info.customPopup) != "undefined") {
                            customPopupLayer = layerId;
                            
                            info.customPopup();

                            $.fancybox({
                                href: '#custom_popup'
                                //modal: true
                            });
                            
                            return false; // only show the custom info dialog for the first layer that has it
                        }
                    });

                    if (customPopupLayer !== null)
                    {
                        //infoPopup.dialog('close');
                        return;
                    }
                    
                    $.each(selectedFeatures, function (layerId, feature) {
                        var table = $("<table>"),
                            info,
                            tr1,
                            td1,
                            tr2,
                            td2,
                            tr3,
                            td3;

                        info = UNREDD.layerInfo[layerId](feature);
                        tr1 = $("<tr/>");
                        td1 = $('<td colspan="2" class="area_name" />');
                        tr1.append(td1);
                        table.append(tr1);
                        table.mouseover(function () {
                            highlightLayer.removeAllFeatures();
                            highlightLayer.addFeatures(feature);
                            highlightLayer.redraw();
                        });
                        table.mouseout(function () {
                            highlightLayer.removeAllFeatures();
                            highlightLayer.redraw();
                        });
                        td1.append(info.title().toLowerCase());

                        tr2 = $("<tr/>");
                        td2 = $("<td class=\"td_left\"/>");
                        tr2.append(td2);
                        table.append(tr2);

                        // TODO: localize statistics and zoom to area buttons
                        td2.append("<a class=\"feature_link fancybox.iframe\" id=\"stats_link_" + layerId + "\" href=\"" + info.statsLink() + "\">Statistics</a>");
                        td3 = $("<td class=\"td_right\"/>");
                        td3.append("<a class=\"feature_link\" href=\"#\" id=\"zoom_to_feature_" + layerId + "\">Zoom to area</a>");
                        tr2.append(td3);
                        infoPopup.append(table);

                        $('#stats_link_' + layerId).fancybox({
                            maxWidth    : 840,
                            maxHeight : 600,
                            fitToView : false,
                            width       : 840,
                            height      : 590,
                            autoSize    : false,
                            closeClick  : false,
                            openEffect  : 'none',
                            closeEffect : 'fade'
                        });

                        if (info.info && info.info()) {
                            tr3 = $("<tr/>");
                            td3 = $("<td class=\"td_left\" colspan=\"2\"/>");
                            tr3.append(td3);
                            table.append(tr3);
                            td3.append(info.info());
                        }


                        $('#drivers_data_link').fancybox({
                            'autoScale': false,
                            'transitionIn': 'none',
                            'transitionOut': 'fade',
                            'type': 'iframe',
                            'scrolling': 'no',
                            'width': 500,
                            'height': 600
                        });

                        $("#zoom_to_feature_" + layerId).click(function () {
                            UNREDD.map.zoomToExtent(feature.geometry.getBounds().scale(1.2));
                        });
                    });
                }

                var totalHeight = 0;

                // If no features selected then close the dialog        
                if (nSelectedFeatures == 0) {
                  infoPopup.dialog('close');
                }
                else {
                    // Don't reposition the dialog if already open
                    if (!infoPopup.dialog('isOpen')) {
                        infoPopup.dialog('option', 'position', [x, y]);

                        // Finally open the dialog
                        infoPopup.dialog('open');
                    }

                    $.each($('#info_popup table'), function (id, elem) {
                        totalHeight += $(elem).height() + 12;
                    });

                    infoPopup.dialog('option', 'height', totalHeight + 35);
                }
            };
        }
    });


    var statsPolygonLayer = new OpenLayers.Layer.Vector("Statistics Polygon Layer");
    UNREDD.map.addLayer(statsPolygonLayer);
    var drawStatsPolygonControl = new OpenLayers.Control.DrawFeature(statsPolygonLayer,
        OpenLayers.Handler.Polygon,
        {
            featureAdded: function (feature) {
                feature.destroy(); // TODO
            }
        }
    );
    UNREDD.map.addControl(drawStatsPolygonControl);
    
    // setup various UI elements
    //$("#toggle_legend").button();
    $("#legend_pane").dialog({
        position: ['right', 'bottom'],
        closeOnEscape: false,
        height: 300,
        //height: 100,
        minHeight: 400,
        maxHeight: 400,
        width: 400,
        zIndex: 2000,
        resizable: false,
        close: function (event, ui) {
            legendOn = false;
        }
    });

    var openLegend = function (scrollToId) {
        if (!legendOn) {
            $("#legend_pane").dialog('open');
        }

        legendOn = true;

        if (scrollToId) {
            $("#legend_pane").animate({ scrollTop: scrollToId.offset().top - $('#legend_pane_content').offset().top }, 'slow');
        }
    }

    var closeLegend = function (scrollToId) {
        $("#legend_pane").dialog('close');
        legendOn = false;
    }

    $("#legend_pane").dialog('close'); // using autoOpen, it doesn't show when you click the button - don't have time
    $("#toggle_legend").click(function () {
        if (!legendOn) {
            openLegend();
        } else {
            closeLegend();
        }
        
        return false;
    });

    $("#layer_list_selector_pane").buttonset();
    $("#layer_list_selector_pane").show();
    
    $("#all_layers").click(function () {
        $("#layers_pane").show();
        $("#active_layers_pane").hide();
    });

    $("#active_layers").click(function () {
        $("#layers_pane").hide();
        /*
        $("#active_layers_pane").dialog({
            closeOnEscape: false,
            resizable: false,
            open: function(event, ui) { $("#layer_list_selector_pane .ui-dialog-titlebar-close", ui.dialog).hide(); }
        });
        */
        $("#active_layers_pane").accordion({
            collapsible: false,
            autoHeight: false,
            animated: false,
            create: function (event, ui) {
                $('#active_layers_pane .ui-icon-triangle-1-s').hide();
                updateActiveLayersPane(mapContexts);
            }
        });
        $("#active_layers_pane").show();
    });

    // Time slider management
    getClosestPastDate = function (date, dateArray) {
        var result = null,
        dateInArray,
        i;

        for (i = 0; i < dateArray.length; i++) {
            dateInArray = dateArray[i];
            if (date >= dateInArray && (result === null || result < dateInArray)) {
                result = dateInArray;
            }
        }

        return result;
    };

    getClosestFutureDate = function (date, dateArray) {
        var result = null,
            dateInArray,
            i;

        for (i = 0; i < dateArray.length; i++) {
            dateInArray = dateArray[i];
            if (date <= dateInArray && (result === null || result > dateInArray)) {
	            result = dateInArray;
            }
        }
        
        return result;
    };
    
    setLayersTime = function (selectedDate) {
        //console.log(layersJsonData); // DEBUG
        // loop through layers to see if they are time dependent
        $.each(UNREDD.timeDependentLayers, function (layerName, layer) {
            var sDates,
                dates = [],
                i,
                d,
                newDate,
                layerInfo = layer.configuration;

            // parse the wmsTime string
            sDates = layerInfo.wmsTime.split(",");
            for (i = 0; i < sDates.length; i++) {
                d = new Date();
                if (d.setISO8601(sDates[i])) {
                    dates.push(d);                	
                }
            }

            if (dates.length) {
	            newDate = getClosestPastDate(selectedDate, dates);
                if (newDate === null) {
                	newDate = getClosestFutureDate(selectedDate, dates);
                }
	            layer.olLayer.mergeNewParams({'time': isoDateString(newDate)});
	            UNREDD.map.events.triggerEvent("changelayer", {
	            	layer: layer.olLayer,
	            	property: "time"
	            });
            }
        });
    };
    
    /**************
    /* Time Slider
     **************/
    // Calculate UNREDD.times from layer configuration
    var timesObj = {};
    for(layer in UNREDD.allLayers) {
    	var layerTimes = UNREDD.allLayers[layer].configuration.wmsTime;
    	if (layerTimes) {
    		layerTimes = layerTimes.split(",");
    		for (i in layerTimes) {
    			var year = new Date(layerTimes[i]).getFullYear();
    			if (!isNaN(year)) {
    				timesObj[year]=0; // Put it in an object to avoid duplicate years.
    			}
    		}
    	}
    }
    for(time in timesObj) {
    	UNREDD.times.push(parseInt(time));
    }
    UNREDD.times.sort();
    
    // Create time slider
    if (UNREDD.times.length) {
	    $("#time_slider_label").text(UNREDD.times[UNREDD.times.length-1]);
	    $("#time_slider").slider({
	        min: 0,
	        max: UNREDD.times.length-1,
	        value: UNREDD.times[UNREDD.times.length-1],
	        slide: function (event, ui) {
	            $("#time_slider_label").text(UNREDD.times[ui.value]);
	        },
	        change: function (event, ui) {
	            var selectedDate = new Date(Date.UTC(UNREDD.times[ui.value], 0, 1));
	            setLayersTime(selectedDate);
	        }
	    });
	
	    // Init layers time
	    year = UNREDD.times[$("#time_slider").slider("value")];
	    selectedDate = new Date(Date.UTC(year, 0, 1));
	    setLayersTime(selectedDate);
    } else {
    	$("#time_slider_pane").hide();
    }
  
    // Info click handler
    infoControl = new OpenLayers.Control.WMSGetFeatureInfo({
        url: UNREDD.wmsServers[0],
        title: 'Identify features by clicking',
        layers: UNREDD.queryableLayers,
        queryVisible: true,
        infoFormat: 'application/vnd.ogc.gml',
        hover: false,
        drillDown: true,
        maxFeatures: 5,
        handlerOptions: {
            "click": {
                'single': true,
                'double': false
            }
        },
        eventListeners: {
            // nogetfeatureinfo: function (e) {
            //     console.log(e); // DEBUG
            // },
            getfeatureinfo: function (evt) {
                if (evt.features && evt.features.length) {
                    showInfo(evt);
                }
            }
        },
        formatOptions: {
            typeName: 'XXX', featureNS: 'http://www.openplans.org/unredd'
        }
    });
    UNREDD.map.addControl(infoControl);
    infoControl.activate();
          
    $("#button_statistics").bind(
        'click',
        function () {
        	if (!$("#button_feedback").hasClass('selected')) { // Prevent activation if feedback is active
	            if (drawStatsPolygonControl.active) {
	                $(this).removeClass('selected');
	                drawStatsPolygonControl.deactivate();
	                infoControl.activate();
	                $("#statistics_info_div").fadeOut(200);
	            } else {
	                $("#button_feedback").removeClass('selected');
	                $(this).addClass('selected');
	                infoControl.deactivate();
	                drawStatsPolygonControl.activate();
	                $("#statistics_info_div").show();
	            }
        	}
            return false;
        }
    );

    $("#disclaimer_popup").fancybox({
        'width': 600,
        'height': 400,
        'autoScale': true,
        'transitionIn': 'fade',
        'transitionOut': 'fade',
        'type': 'ajax'
    });
    
    UNREDD.map.addLayers(UNREDD.visibleLayers);
    //var wikimapia = new OpenLayers.Layer.Wikimapia( "Wikimapia",
    //  {sphericalMercator: true, isBaseLayer: false, 'buffer': 0 } );
    //map.addLayer(wikimapia);
    
    // StyleMap for the highlight layer
    styleMap = new OpenLayers.StyleMap({'strokeWidth': 5, fillOpacity: 0, strokeColor: '#ee4400', strokeOpacity: 0.5, strokeLinecap: 'round'});
    highlightLayer = new OpenLayers.Layer.Vector("Highlighted Features", {styleMap: styleMap});
    UNREDD.map.addLayer(highlightLayer);

    
    /**************/
    /** Feedback **/
    /**************/

    // Feedback button
    $("#button_feedback").bind(
        'click',
        function () {
        	if (!$("#button_statistics").hasClass('selected')) { // Prevent activation if statistics is active
	            if ($(this).hasClass('selected')) {
	                $(this).removeClass('selected');
	            } else {
	                $(this).addClass('selected');
	            	$("#button_statistics").removeClass('selected');
	                openDialog();
	            }
        	}
            return false;
        }
    );
    
    // Feedback vector layer
    feedbackLayer = new OpenLayers.Layer.Vector("Feedback");  
    UNREDD.map.addLayer(feedbackLayer);
    
    // Feedback form
	function openDialog() {
		Recaptcha.reload();			
	
		$("#feedback_submit").button();
        $("#feedback_submit").click(function () {
    	    var mailRegex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    	    if(!mailRegex.test($("#email_").val())) {
    			$("#feedback-invalid-mail").dialog({
    				height: 140,
    				modal: true,
    	            resizable: false,
    	            buttons: { "Ok": function () { $(this).dialog( "close" ); } }
    			});
    	    } else {
    	    	// Prepare params for submit
    	    	var olLayer = UNREDD.allLayers[$("#fb_layers").val()].olLayer;
    	    	var layerDate = (olLayer.params && olLayer.params.TIME);
    	    	var params = {
    	    		"recaptcha_challenge": $("#recaptcha_challenge_field").val(),
    	    		"recaptcha_response": $("#recaptcha_response_field").val(),
	    	    	"LayerName": (olLayer.params && olLayer.params.LAYERS) || olLayer.name,
	    	    	"UserName": $('#name_').val(),
	    	    	"UserMail": $('#email_').val()
    	    	};
    	    	if (layerDate) {
    	    		params.layerDate = Math.round(new Date(layerDate).getTime() / 1000);
    	    	};

    	    	// Do submit
    	    	$.ajax({
    	    	    type: 'POST',
    	    	    contentType: 'application/json',
    	    	    url: 'feedback?' + $.param(params),
    	    	    data: JSON.stringify({
    	    	    	"text": $('#feedback_').val(),
    	    	    	"geo": UNREDD.fb_toolbar.getFeaturesAsGeoJson()
    	    	    }),
    	    	    dataType: "json",
    	    	    success: function(data, textStatus, jqXHR) {
	    	    		alert(messages[data.message]);
	    	    		$("#feedback_popup").dialog('close');
    	    	    },
    	    	    error: function(jqXHR, textStatus, errorThrown) {
    	    			Recaptcha.reload();
    	    			try {
    	    				var response = $.parseJSON(errorThrown);
    	    			} catch(e) {}
    	    	    	if (response) {
    	    	    		alert(messages[response.message]);
    	    	    	} else {
    	    	    		alert(messages.ajax_feedback_error);
    	    	    	}
	    	    	}
    	    	});
    	    }
        });

        $("#feedback_cancel").button();
        $("#feedback_cancel").click(function () {
            $("#feedback_popup").dialog('close');
        });
        
        $("#fb_layers").change(function(evt) {
        	var layerId = $("#fb_layers").val();
        	
        	// Determine if layer is queryable
        	var queryable = false;
        	$.each(UNREDD.mapContexts[layerId].layers, function(index, layer) {
        		if(layer.configuration.queryable) {
        			queryable = layer.olLayer;
        		}
        	});
        	if (UNREDD.fb_toolbar) {
        		UNREDD.fb_toolbar.setQueryable(queryable);
        	}
			
			// Determine if layer is time-varying
			var olLayer = UNREDD.allLayers[layerId].olLayer;
			if (olLayer.params && olLayer.params.TIME) {
				var date = new Date(olLayer.params.TIME);
				$("#fb_time").html(messages.feedback_year + " " + date.getFullYear());
			} else {
				$("#fb_time").html("");
			}
			
        });

        $("#feedback_popup").dialog({
            closeOnEscape: false,
            width: 340,
            height: 415,
            zIndex: 2000,
            resizable: false,
            position: [270, 150],
            title: messages.feedback_title,
            
            open: function (event, ui) {
            	// Empty form
                $('#name_').val('');
                $('#email_').val('');
                $('#feedback_').val('');
            	$('#fb_layers').empty();
            	
            	// Add draw toolbar
                infoControl.deactivate();
                UNREDD.fb_toolbar = new OpenLayers.Control.PortalToolbar(feedbackLayer, {div: document.getElementById("fb_toolbar")});
                UNREDD.map.addControl(UNREDD.fb_toolbar);
                           	
                // Inform layers combo
            	$.each(UNREDD.allLayers, function(key, layer) {
            		if(layer.olLayer.visibility == true && UNREDD.mapContexts[layer.name]) {
            			var name = layer.name;
            			var label = UNREDD.mapContexts[name].configuration.label;
                		$("#fb_layers").append('<option value="'+name+'">'+label+'</option>');
            		}
            	});
				$("#fb_layers").change();
            	
				// Sync layers combo with visible layers
            	UNREDD.map.events.on({
            		"changelayer": function(evt) {
            			if (evt.property == "visibility") {
            				var layer = evt.layer;
        					var name = layer.name;
            				if(evt.layer.visibility == false) {
            					$("#fb_layers option[value='"+name+"']").remove();
            				} else {
            					var label = UNREDD.mapContexts[name].configuration.label;
            					$("#fb_layers").append('<option value="'+name+'">'+label+'</option>');
            				}
            				$("#fb_layers").change();
            			}
            			if (evt.property == "time" && evt.layer.name == $("#fb_layers").val()) {
            				var date = new Date(evt.layer.params.TIME);
            				$("#fb_time").html(messages.feedback_year + " " + date.getFullYear());
            			}
            		}
            	});
            },
            
            close: function (event, ui) {
                $("#button_feedback").removeClass('selected');
                UNREDD.map.removeControl(UNREDD.fb_toolbar);
                UNREDD.fb_toolbar.deactivate();
                UNREDD.fb_toolbar.destroy();
                UNREDD.fb_toolbar=null;
                infoControl.activate();
                feedbackLayer.removeAllFeatures();
            }
        });
	};
    
    UNREDD.map.setCenter(UNREDD.mapCenter, UNREDD.defaultZoomLevel);
       
    UNREDD.map.addControl(new OpenLayers.Control.Navigation());
    
    if (!UNREDD.map.getCenter()) {
        UNREDD.map.zoomToMaxExtent();
    }
});
