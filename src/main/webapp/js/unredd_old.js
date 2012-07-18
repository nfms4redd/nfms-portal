// TODO:
// + Legends
//   + Save the following GeoServer legends in the legend folder
//       + Deforestation
//       + REDD+ projects
//       + REDD+ initiatives
//       + Logging concessions
//       + Protected areas
//       + Check legend translation after removing lines ~463...
// + Put back info button on context group headers
// + Re-implement time slider
// + Fix the error GET http://www.rdc-snsf.org/new/img/blank.gif 404 (Not Found) (from Safari's console)
// ? Hover info popup instead of dialog
// ~ Check with Internet Explorer - in progress while developing
// ~ Add active layers pane with transparency slider for each layer - slider added - improve layout
// - Query info dialog - put info in layers.json
// - Fix layout bug in layes div when mixing layers with inline legend and layers with no inline legend
// - Editing of features


/*
(function($,undefined){
    // jquery.ui.widget
    $.Widget.prototype._createWidget = function( options, element ) {
                // $.widget.bridge stores the plugin instance, but we do it anyway
                // so that it's stored even before the _create function runs
                $.data( element, this.widgetName, this );
                this.element = $( element );
                this.options = $.extend( true, {},
                        this.options,
                        this._getCreateOptions(),
                        options );

                var self = this;
                // <--- memory leak
                //this.element.bind( "remove." + this.widgetName, function() {
                //      self.destroy();
                //});

                // <--- workaround -- start
                this.element.bind( "remove." + this.widgetName,
                                                     {widgetName: this.widgetName},
                                                     this.__destroy);
                // <--- workaround -- end

                this._create();
                this._trigger( "create" );
                this._init();
    };
    // new __destroy method
    $.Widget.prototype.__destroy = function(e){
        var self = $(this).data(e.data.widgetName);
        self.destroy();
    }

})(jQuery);
*/

var UNREDD = {};

var isoDateString,
    //i18nInit,
    map;

Date.prototype.setISO8601 = function (string) {
    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
            "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\\.([0-9]+))?)?" +
            "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?",
        d = string.match(new RegExp(regexp)),
        offset = 0,
        date = new Date(d[1], 0, 1),
        time;

    if (d[3]) {date.setMonth(d[3] - 1);}
    if (d[5]) {date.setDate(d[5]);}
    if (d[7]) {date.setHours(d[7]);}
    if (d[8]) {date.setMinutes(d[8]);}
    if (d[10]) {date.setSeconds(d[10]);}
    if (d[12]) {date.setMilliseconds(Number("0." + d[12]) * 1000);}
    if (d[14]) {
        offset = (Number(d[16]) * 60) + Number(d[17]);
        offset *= ((d[15] === '-') ? 1 : -1);
    }

    offset -= date.getTimezoneOffset();
    time = (Number(date) + (offset * 60 * 1000));
    this.setTime(Number(time));
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

/*
i18nInit = function () {
    // This will initialize the plugin
    jQuery.i18n.properties({
        name: 'Messages',
        path: 'js/bundles',
        mode: 'map',
        language: 'es',
        callback: function () {}
    });
};
*/

$(document).ready(function () {
    // disable text selection on Explorer
    document.body.onselectstart = function() { return false; };
});

$(window).load(function () {
    var visibleLayersArray   = [],
        queryableLayersArray = [],
        allLayers = {}, // at the moment, only used to set time for the wms time
        layersJsonData, // at the moment, only used to set time for the wms time
        options,
        styleMap, // TODO: check if the following ones can be local to some function
        highlightLayer,
        //loadBundles,
        //layerInfo, // TODO: this will be substituted by configuration in the JSON file
        showInfo,
        setLayersTime,
        selectedDate,
        click,
        legendOn = false,
        year,
        markers,
        infoControl,
        //feedback = false, // TODO
        getClosestPastDate,
        //languageCode = 'es',
        //showInfoOnClick = true, // TODO
        updateActiveLayersPane,
        //mapTool = "info",
        mapContexts = {},
        langData;

    // Set map div height
    var setMapDivSize = function () {
        var bannerHeight = $('#header').height();
        $('#map').css('top', bannerHeight);
        $('#map').css('height', $(window).height() - bannerHeight);
        $('#map').css('width', $(window).width());
    }
    
    setMapDivSize();
    
    $(window).resize(function() {
        setMapDivSize();
    });
    
    options = {
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        units: "m",
        //numZoomLevels: 18,
        maxResolution: UNREDD.maxResolution,
        maxExtent: UNREDD.maxExtent,
        restrictedExtent: UNREDD.restrictedExtent,
        allOverlays: true,
        controls: [
            new OpenLayers.Control.Navigation({ documentDrag: true, zoomWheelEnabled: false }),
            new OpenLayers.Control.Scale()
        ]
    };

    map = new OpenLayers.Map('map', options);

    // Load the JSON layers, contexts, and context grouping definition from the JSON object
    //$.getJSON('data/layers.json', function(data) {
    $.ajax({url: 'layers.json', dataType: 'json', async: false, success: function (data) {
        var wmsLayers = {},
            setContextVisibility,
            setupAllContexts,
            setLegends,
            loadContextGroups;

        layersJsonData = data;
        
        // set the langData variable (used to localized content dynamically created
        // in Javascript
        langData = data.lang;
        
        setContextVisibility = function (contextName, mapContext, active) {
            jQuery.each(mapContext.layers, function (layerName, layer) {
                layer.setVisibility(active);
                mapContext.active = active;
            });
            
            var icon = $('#' + contextName + '_inline_legend_icon');
            if (active) {
                icon.addClass('on');
                icon.click(function (event) {
                    openLegend($('#' + contextName + '_legend'));
                    event.stopPropagation();
                    return false;
                })
            } else {
                icon.removeClass('on');
                icon.off('click');
                icon.click(function (event) {
                    event.stopPropagation();
                    return false;
                })
            }
        };

        setupAllContexts = function (contextsDefinition, mapContexts) {
            // look for active contexts
            jQuery.each(contextsDefinition, function (contextName, contextDef) {
                var active = typeof contextDef.active !== 'undefined' && contextDef.active === "true";
                if (mapContexts[contextName]) {
                    // If the context has no layers (this is a temporary situation)
                    // the mapContext object was not created
                    setContextVisibility(contextName, mapContexts[contextName], active);
                }
            });
        };

        updateActiveLayersPane = function (mapContexts) {
            var //div,
                table, tr, td, td2, layers, inlineLegend, transparencyDiv;

            // empty the active_layers div (layer on the UI -> context here)
            $('#active_layers_pane div').empty();

            table = $('<table style="width:90%;margin:auto"></table>');
            $('#active_layers_pane div').append(table);

            jQuery.each(mapContexts, function (contextName, context) {
                if (context.active) {
                    // First row: inline legend and context name
                    tr = $('<tr></tr>');

                    if (data.contexts[contextName].hasOwnProperty('inlineLegendUrl')) {
                        td = $('<td style="width:20px"></td>');
                        inlineLegend = $('<img class="inline-legend" src="' + data.contexts[contextName].inlineLegendUrl + '">');
                        td.append(inlineLegend);
                        tr.append(td);
                        td2 = $('<td></td>');
                    /*
                    } else if (data.contexts[contextName].hasOwnProperty('legendUrl')) {
                        td = $('<td style="width:20px"></td>');
                        inlineLegend = $('<img class="inline-legend" src="images/legend_off.gif">');
                        td.append(inlineLegend);
                        tr.append(td);
                    */
                    } else {
                        td2 = $('<td colspan="2"></td>');
                    }
                    td2.append(context.label);
                    tr.append(td2);
                    table.append(tr);

                    // Another row
                    tr = $('<tr></tr>');
                    transparencyDiv = $('<div style="margin-top:4px; margin-bottom:12px;" id="' + contextName + '_transparency_slider"></div>');
                    td = $('<td colspan="2"></td>');
                    td.append(transparencyDiv);
                    tr.append(td);
                    table.append(tr);

                    layers = mapContexts[contextName].layers;

                    (function (contextLayers) {
                        $(transparencyDiv).slider({
                            min: 0,
                            max: 100,
                            value: 100,
                            slide: function (event, ui) {
                                $.each(contextLayers, function (n, layer) {
                                    layer.setOpacity(ui.value / 100);
                                });
                            }
                        });
                    }(layers));
                }
            });
        };

        // Add the legend images to the legend pane.
        // This implementation works only if two contexts don't have a layer in common.
        // A better implementation would have to scan all the active contexts and see which layers should be visible
        setLegends = function (contextName, contextDef, layerDefs, contextIsActive) {
            jQuery.each(contextDef.layers, function (n, layerName) {
                var //legendFile,
                    table,
                    layerDef = layerDefs[layerName],
                    legendName;

                if (layerDef.visible === "true" && typeof layerDef.legend !== "undefined") {
                    //legendFile = layerDef.legend;
                    legendName = contextName + '_legend';

                    if (!contextIsActive) {
                        $('#' + legendName).remove();
                    } else {
                        table  = '<table class="layer_legend" id="' + legendName + '">';
                        table += '<tr class="legend_header">';
                        table += '<td class="layer_name">' + layerDef.label + '</td>';
                        
                        if (typeof layerDef.sourceLink !== "undefined") {
                            // TODO: localize data source link
                            table += '<td class="data_source_link"><span class="lang" id="data_source">Data source:</span> <a target="_blank" href="' + layerDef.sourceLink + '">' + layerDef.sourceLabel + '</a></td>';
                        } else {
                            table += "<td></td>";
                        }
                        table += '</tr>';
                        table += '<tr class="legend_image">';
                        table += '<td colspan="2" style="width:100%;background-color:white"><img src="loc/' + languageCode + '/images/' + layerDef.legend + '" /></td>';
                        table += '</tr>';
                        table += '</table>';
                    }

                    $('#legend_pane_content').append(table);
                }
            });
        };

        var contextHasLegend = function (contextName)
        {
            var contextDef = layersJsonData.contexts[contextName];
            for (i in contextDef.layers) {
                var layerName = contextDef.layers[i];
                if (layersJsonData.layers[layerName].hasOwnProperty('legend')) return true
            }
            
            return false;
        }
        
        // Though recursive and ready for n level groupings with some adjustments, this function
        // is meant to work with three level grouping of contexts
        loadContextGroups = function (contextGroups, level, element) {
            jQuery.each(contextGroups.items, function (contextGroupName, contextGroupDefinition) {
                var innerElement = null,
                    accordionHeader,
                    contextsDiv,
                    header,
                    contextName,
                    tr,
                    td1,
                    td3,
                    td4,
                    label,
                    infoButton,
                    inlineLegend,
                    active;

                
                if (contextGroupDefinition.hasOwnProperty('group')) {
                    // it's a group
                    if (level === 0) {
                        if (typeof contextGroupDefinition.group.infoFile !== 'undefined') {
                            accordionHeader = $('<div style="position:relative" class="accordion_header"><a style="width:190px" href="#">' + contextGroupDefinition.group.label
                                + '</a></div>');
                            infoButton = $('<a style="position:absolute;top:3px;right:7px;width:16px;height:16px;padding:0;" class="layer_info_button" href="loc/' + languageCode + '/html/' + contextGroupDefinition.group.infoFile + '"></a>')
                            accordionHeader.append(infoButton);
                            
                            infoButton.fancybox({
                                'autoScale' : false,
                                'openEffect' : 'elastic',
                                'closeEffect' : 'elastic',
                                'type': 'ajax',
                                'overlayOpacity': 0.5
                            });
                            
                            // prevent accordion item from expanding when clicking on the info button
                            infoButton.click(function (event) {
                                event.stopPropagation();
                            });
                        } else {
                            accordionHeader = $("<div class=\"accordion_header\"><a href=\"#\">" +  contextGroupDefinition.group.label + "</a></div>");
                        }
                        $("#layers_pane").append(accordionHeader);
                        contextsDiv = $("<div class=\"context_buttonset\"></div>");
                        innerElement = $('<table style="width:100%;border-collapse:collapse"></table>');

                        contextsDiv.append(innerElement);
                        $("#layers_pane").append(contextsDiv);
                    } else {
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
                        active = data.contexts[contextName].active === "true"
                        
                        if (typeof data.contexts[contextName] !== "undefined" && typeof data.contexts[contextName].layers !== "undefined") {
                            tr = $('<tr class="layer_row">');
                            //if (active) {
                            //  tr.addClass('active');
                            //}
                            
                            if (data.contexts[contextName].hasOwnProperty('inlineLegendUrl')) {
                                // context has an inline legend
                                td1 = $('<td style="width:20px">');
                                inlineLegend = $('<img class="inline-legend" src="' + data.contexts[contextName].inlineLegendUrl + '">');
                                td1.append(inlineLegend);
                            } else if (contextHasLegend(contextName)) {
                                // context has a legend to be shown on the legend pane
                                // add link to show the legend pane
                                if (active) {
                                    td1 = $('<td style="font-size:9px;width:20px;height:20px"><a id="' + contextName + '_inline_legend_icon" class="inline_legend_icon on"></a></td>');
                                    // Add the legend to the legend pane (hidden when page loads)
                                    setLegends(contextName, layersJsonData.contexts[contextName], layersJsonData.layers, true);
                                } else {
                                    td1 = $('<td style="font-size:9px;width:20px;height:20px"><a id="' + contextName + '_inline_legend_icon" class="inline_legend_icon"></a></td>');
                                }
                            } else {
                                td1 = $('<td></td>');
                            }
                            
                            var checkbox = $('<div class="checkbox" id="' + contextName + "_checkbox" + '"></div>');
                            if (active) {
                                checkbox.addClass('checked');
                            }
                            var td2 = $('<td style="width:16px"></td>');
                            td2.append(checkbox);
                            
                            td3 = $('<td style="color:#FFF">');
                            td3.text(data.contexts[contextName].label);
                            td4        = $('<td style="width:16px;padding:0">');
                            infoButton = $('<a class="layer_info_button" id="' + contextName + '_info_button" href="loc/' + languageCode + '/html/' + data.contexts[contextName].infoFile + '"></a>');
                            
                            if (typeof data.contexts[contextName].infoFile !== 'undefined') {
                                td4.append(infoButton);
                            }
                            if (td1) {tr.append(td1);}
                            tr.append(td2, td3, td4);

                            element.append(tr);
                            
                            // The :hover pseudo-selector on non-anchor elements is known to make IE7 and IE8 slow in some cases
                            /*
                            tr.mouseenter(function () {
                                tr.addClass('hover');
                            }).mouseleave(function () {
                                tr.removeClass('hover');
                            });
                            */
                            
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
                                    
                                    var active = !mapContexts[contextName].active;
                                    setContextVisibility(contextName, mapContexts[contextName], active);
                                    setLegends(contextName, data.contexts[contextName], data.layers, active);
                                    updateActiveLayersPane(mapContexts);
                                });
                            }(checkbox))
                            
                        } else if (typeof data.contexts[contextName] !== "undefined") {
                            tr               = $('<tr style="font-size:10px;height:22px">');
                            td1              = $('<td colspan="3" style="color:#FFF">');
                            td2              = $('<td style="width:16px;padding:0">');
                            
                            td1.text(data.contexts[contextName].label);
                            
                            infoButton = $('<a class="layer_info_button" id="' + contextName + '_info_button" href="loc/' + languageCode + '/html/' + data.contexts[contextName].infoFile + '"></a>');

                            td2.append(infoButton);
                            tr.append(td1, td2);
                            element.append(tr);
                        }
                        
                        infoButton.fancybox({
                            'autoScale' : false,
                            'openEffect' : 'elastic',
                            'closeEffect' : 'elastic',
                            'type': 'ajax',
                            'overlayOpacity': 0.5
                        });
                    }
                }
            });
        };

        // Set wmsLayers = { layer_name1: layer_object1, layer_name2: layer_object2 ... }
        jQuery.each(data.layers, function (layerName, layerDefinition) {
            //console.log(layerName + ' - ' + layerDefinition.visible); // DEBUG
            var baseUrl = layerDefinition.baseUrl;
            var urls = [
                baseUrl, // this must go first as it is used for the getfeatureinfo request
                "http://unredd.geo-solutions.it" + baseUrl,
                "http://www.rdc-snsf.org" + baseUrl,
                "http://84.33.1.31" + baseUrl
            ];
            
            urls = [baseUrl]; // DEBUG
            
            var wmsParams = {layers: layerDefinition.wmsName, format: layerDefinition.imageFormat, transparent: true};
            
            // Add parameters defined in layers.json
            var wmsParameters = layerDefinition.wmsParameters;
            for (var paramName in wmsParameters) {
                if (wmsParameters.hasOwnProperty(paramName)) {
                    wmsParams[paramName] = wmsParameters[paramName];
                }
            }
            
            var layer = new OpenLayers.Layer.WMS(
                layerName,
                urls,
                wmsParams,
                {transitionEffect: "resize", removeBackBufferDelay: 0, isBaseLayer: false, 'buffer': 0, visibility: layerDefinition.visible === 'true', projection: 'EPSG:900913', noMagic: true}
            );

            wmsLayers[layerName] = layer;

            if (layerDefinition.visible === "true") {
                visibleLayersArray.push(layer);
            }
            allLayers[layerName] = layer;
            
            if (layerDefinition.queryable) { queryableLayersArray.push(layer); }
        });

        // Load the contexts from the JSON object
        jQuery.each(data.contexts, function (contextName, contextDefinition) {
            if (contextDefinition.layers) {
                // if there's no layer property in the contextDefinition.layers, it's a stub for later use
                var contextLayers = {};
                mapContexts[contextName] = {};

                jQuery.each(contextDefinition.layers, function (id, layerName) {
                    contextLayers[layerName] = wmsLayers[layerName];
                });

                mapContexts[contextName].layers = contextLayers;
                mapContexts[contextName].label = contextDefinition.label;
                //console.log(mapContexts);
            }
        });
        
        //var fragment = document.createDocumentFragment();
        loadContextGroups(data.contextGroups, 0, null);

        //$("#layers_pane").accordion({collapsible: true, autoHeight: false, animated: false});
        $("#layers_pane").accordion({ collapsible: true, autoHeight: false, header: ".accordion_header", animated: false } );
        $("#layers_pane").show();
        
        setupAllContexts(data.contexts, mapContexts);

    // create info dialog
    var selectedFeatures = {};
    $("#info_popup").dialog({
        closeOnEscape: true,
        height: 170,
        minHeight: 400,
        maxHeight: 400,
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
            infoPopup = $("#info_popup");

        highlightLayer.destroyFeatures();
        //$("#piemenu").hide();
        
        if (evt.features && evt.features.length) {
            // re-project to Google projection
            for (i = 0; i < evt.features.length; i++) {
                evt.features[i].geometry.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
                feature = evt.features[i];
                featureType = feature.gml.featureType;
                selectedFeatures[featureType] = feature;
            }

            infoPopup.empty();

            /*
            $.each(data.statistics, function (statisId, statsDescription) {
                
            });
            */
            
            $.each(selectedFeatures, function (layerId, feature) {
                var table = $("<table>"),
                    info,
                    tr1,
                    td1,
                    tr2,
                    td2,
                    tr3,
                    td3;
                
                //console.log(data.statistics);
                //console.log(layerId);
                
                info = UNREDD.layerInfo[layerId](feature);
                
                /*
                $.each(data.statistics, function (statisId, statsDescription) {
                });
                */
                
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
                    //console.log(feature); // DEBUG
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
                //$("#piemenu").append("</div>");
                //$('#stats_link').attr('href', 'data/charts/' + evt.features[0].attributes.SECTEUR + '.html').fancybox({
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

                /*
                $('#drivers_data_link').fancybox({
                    'autoScale' : false,
                    'transitionIn' : 'none',
                    'transitionOut' : 'fade',
                    'type' : 'iframe',
                    'scrolling' : 'no',
                    'width' : 500,
                    'height' : 600
                });
                */
                
                $("#zoom_to_feature_" + layerId).click(function () {
                    map.zoomToExtent(feature.geometry.getBounds().scale(1.2));
                });
            });

            /*
            //$('#stats_link').attr('href', 'data/charts/' + evt.features[0].attributes.SECTEUR + '.html').fancybox({
            $('#stats_link').attr('href', 'data/charts/' + evt.features[0].attributes.OBJECTID + '.html?name=' + evt.features[0].attributes.SECTEUR).fancybox({
                'autoScale' : false,
                'transitionIn' : 'fade',
                'transitionOut' : 'fade',
                'type' : 'iframe',
                'scrolling' : 'no',
                'width' : 860,
                'height' : 560
            });
            */

            //$("#piemenu").css("top", y + "px");
            //$("#piemenu").css("left", x + "px");
            //$("#piemenu").show();
            /*
            $("#info_popup").dialog({
                position: [x, y],
                closeOnEscape: true,
                height: 170,
                minHeight: 400,
                maxHeight: 400,
                width: 300,
                zIndex: 2000,
                resizable: false,
                close: function (event, ui) {
                    // destroy all features
                    $.each(selectedFeatures, function (layerId, feature) {
                        feature.destroy();
                    });
                }
            });
            */
            
            if (!infoPopup.dialog('isOpen')) {
                infoPopup.dialog('option', 'position', [x, y]);
            }
            infoPopup.dialog('open');
        }
    };

    }});

    
    var statsPolygonLayer = new OpenLayers.Layer.Vector("Statistics Polygon Layer");
    map.addLayer(statsPolygonLayer);
    var drawStatsPolygonControl = new OpenLayers.Control.DrawFeature(statsPolygonLayer,
            OpenLayers.Handler.Polygon,
            {
                    featureAdded: function (feature) {
                        feature.destroy(); // TODO
                    }
            }
    );
    map.addControl(drawStatsPolygonControl);
    
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
            diff,
            dateInArray,
            i;

        for (i = 0; i < dateArray.length; i++) {
            //console.log(" - " + dateArray[i]); // DEBUG
            dateInArray = dateArray[i];
            if (date >= dateInArray && (undefined === diff || date - dateInArray < diff)) {
                result = dateInArray;
            }
        }

        return result;
    };

    setLayersTime = function (selectedDate) {
        //console.log(layersJsonData); // DEBUG
        // loop through layers to see if they are time dependent
        $.each(layersJsonData.layers, function (layerName, layerInfo) {
            var sDates,
                dates = [],
                i,
                d,
                newDate;

            //console.log(layerInfo.wmsTime); // DEBUG
            if (typeof layerInfo.wmsTime !== 'undefined') {
                // parse the wmsTime string
                sDates = layerInfo.wmsTime.split(",");
                for (i = 0; i < sDates.length; i++) {
                    d = new Date();
                    d.setISO8601(sDates[i]);
                    dates[i] = d;
                }

                newDate = getClosestPastDate(selectedDate, dates);
                allLayers[layerName].mergeNewParams({'time': isoDateString(newDate)});
            }
        });
    };

    $("#time_slider").slider({
        min: UNREDD.minTime,
        max: UNREDD.maxTime,
        value: UNREDD.currentTime,
        step: UNREDD.timeStep,
        slide: function (event, ui) {
            $("#time_slider_label").text(ui.value); //; + '-' + (ui.value + 5));
        },
        change: function (event, ui) {
            var selectedDate = new Date(Date.UTC(ui.value, 0, 1)),
                year;

            setLayersTime(selectedDate);

            // TODO - need to make this parametric
            //year = ui.value + 5;
            //if (year > 2010) {
            //  year = 2010;
            //}
            //deforestation.mergeNewParams({'styles': 'deforestation_temp_' + (year)}); // hack, couldn't do with time dimension in geoserver
        }
    });

    /*
    $("#time_slider_pane").hover(function () {
        $('#time_slider_pane').animate({"opacity": 0.66}, 0);
    },function () {
        $('#time_slider_pane').animate({"opacity": 0.33}, 330);
    });
    */

    /*
    $("#transparency_slider").slider({
        min: 0,
        max: 200,
        value: 100,
        slide: function (event, ui) {
            var layers = map.layers, // WARNING - this hides the other layers variable
                transpValue = ui.value / 100;

            if (ui.value > 80 && ui.value < 120 && ui.value !== 100) {
                // snap to middle position
                $("#transparency_slider").slider('value', 100);

                $.map(layers, function (layer) {
                    if (layer.isBaseLayer && !layer.isVector) {
                        layer.setOpacity(1);
                    }
                });
                return false;
            }

            if (transpValue <= 1) {
                $.map(layers, function (layer) {
                    if (layer.isBaseLayer && !layer.isVector) {
                        layer.setOpacity(transpValue);
                    } else if (!layer.isVector) {
                        layer.setOpacity(1);
                    }
                    / *
                    else if (layer.isVector)
                    {
                        console.log(layer);
                        if (layer.vectorMode) {
                            OpenLayers.Layer.Vector.prototype.setOpacity.apply(layer, [ui.value / 100]);
                        }
                        else {
                            OpenLayers.Layer.Markers.prototype.setOpacity.apply(layer, [ui.value / 100]);
                        }
                    }
                    * /
                });
            } else {
                $.map(layers, function (layer) {
                    if (!layer.isBaseLayer && !layer.isVector) {
                        layer.setOpacity(2 - transpValue);
                    } else if (!layer.isVector) {layer.setOpacity(1);}
                });
            }
        }
        //forestCoverChange.setOpacity(ui.value / 100);
    });
    */

    // Init layers time
    year = $("#time_slider").slider("value");
    selectedDate = new Date(Date.UTC(year, 0, 1));
    setLayersTime(selectedDate);
    // hack - need to re-rasterize deforestation using gdal (instead of GRASS)
    //if (year > 2010) {
    //  year = 2010;
    //}
    //deforestation.mergeNewParams({'styles': 'deforestation_temp_' + (year)}); // hack, couldn't do with time dimension in geoserver

    var FeedbackControl = OpenLayers.Class(OpenLayers.Control, {
        defaultHandlerOptions: {
            'single': true,
            'double': false,
            'pixelTolerance': 0,
            'stopSingle': false,
            'stopDouble': false
        },

        initialize: function (options) {
            this.handlerOptions = OpenLayers.Util.extend({}, this.defaultHandlerOptions);
            OpenLayers.Control.prototype.initialize.apply(this, arguments);
            this.handler = new OpenLayers.Handler.Click(
                this,
                {
                    'click': this.trigger
                },
                this.handlerOptions
            );
        },

        trigger: function (e) {
            var position,
                size,
                offset,
                icon,
                markersLayer,
                marker,
                x,
                y;

            //if (mapTool === "feedback") {
                        if (true) { // DEBUG
                $("#feedback_info_div").fadeOut(200);

                //var lonlat = map.getLonLatFromViewPortPx(e.xy);
                //var position = this.events.getMousePosition(e);

                // place marker
                position = map.getLonLatFromPixel(e.xy);
                size = new OpenLayers.Size(21, 25);
                offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
                icon = new OpenLayers.Icon('js/OpenLayers-2.11/img/marker.png', size, offset);
                markersLayer = map.getLayer('markers');
                marker = new OpenLayers.Marker(position, icon);

                markersLayer.addMarker(marker);

                $("#feedback_submit").click(function () {
                    $("#feedback_popup").dialog('close'); // TODO: add real submit action
                });

                $("#feedback_cancel").click(function () {
                    $("#feedback_popup").dialog('close');
                    markersLayer.removeMarker(marker);
                    //marker.destroy();
                });

                x = e.xy.x + 20;
                y = e.xy.y - 200;

                $("#feedback_popup").dialog({
                    position: [x, y],
                    closeOnEscape: false,
                    //height: 180,
                    minHeight: 400,
                    maxHeight: 400,
                    width: 340,
                    height: 330,
                    zIndex: 2000,
                    resizable: false,
                    open: function (event, ui) {
                        position.transform(new OpenLayers.Projection("EPSG:900913"), new OpenLayers.Projection("EPSG:4326"));

                        $("#fb_coord_x").text(position.lon.toFixed(2));
                        $("#fb_coord_y").text(position.lat.toFixed(2));
                        $('#name').val('');
                        $('#email').val('');
                        $('#feedback').val('');
                        //feedback = false;
                        feedbackControl.deactivate();
                    },
                    close: function (event, ui) {
                        $("#button_feedback").removeClass('selected');
                        feedbackControl.deactivate();
                        infoControl.activate();
                    }
                });
            }
        }
    });

    feedbackControl = new FeedbackControl;
    map.addControl(feedbackControl);
    //feedbackControl.activate();

    // Info click handler
    infoControl = new OpenLayers.Control.WMSGetFeatureInfo({
        url: 'http://localhost/geoserver/wms',
        title: 'Identify features by clicking',
        layers: queryableLayersArray,
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
            /*
            nogetfeatureinfo: function (e) {
                //alert(e); // DEBUG
            },
            */
            getfeatureinfo: function (evt) {
                //if (mapTool === "info") {
                //if (true) { // DEBUG
                if (evt.features && evt.features.length) {
                    showInfo(evt);
                }
            }
        },
        formatOptions: {
            typeName: 'XXX', featureNS: 'http://www.openplans.org/unredd'
        }
    });
    map.addControl(infoControl);
    infoControl.activate();
    
    /*
    var navicationControl = new OpenLayers.Control.Navigation({
        documentDrag: true,
        zoomWheelEnabled: false
        //dragPanOptions: {
        //      enableKinetic: true
        //}
    });
    map.addControl(navicationControl);
    navicationControl.activate();
    */

    /*
    $(".lang_button").bind(
        'click',
        function (event) {
            var lang_code = this.id.substring(7); // button id is button_[lang_code]
            loadBundles(lang_code);

            $('.lang_button').removeClass('selected');

            $(this).addClass('selected');
            
            return false;
        }
    );

    loadBundles('es');
    */
   
    $("#button_feedback").bind(
        'click',
        function () {
            if (feedbackControl.active) {
                $(this).removeClass('selected');
                $("#feedback_info_div").fadeOut(200);
                feedbackControl.deactivate();
                drawStatsPolygonControl.deactivate();
                infoControl.activate();
            } else {
                $("#button_statistics").removeClass('selected');
                $(this).addClass('selected');
                $("#feedback_info_div").show();
                infoControl.deactivate();
                drawStatsPolygonControl.deactivate();
                feedbackControl.activate();
            }
            
            return false;
        }
    );
        
    $("#button_statistics").bind(
        'click',
        function () {
            if (drawStatsPolygonControl.active) {
                $(this).removeClass('selected');
                drawStatsPolygonControl.deactivate();
                feedbackControl.deactivate();
                infoControl.activate();
                $("#statistics_info_div").fadeOut(200);
            } else {
                $("#button_feedback").removeClass('selected');
                $(this).addClass('selected');
                                infoControl.deactivate();
                feedbackControl.deactivate();
                drawStatsPolygonControl.activate();
                $("#statistics_info_div").show();
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
    
    map.addLayers(visibleLayersArray);
    //var wikimapia = new OpenLayers.Layer.Wikimapia( "Wikimapia",
    //  {sphericalMercator: true, isBaseLayer: false, 'buffer': 0 } );
    //map.addLayer(wikimapia);
    
    // StyleMap for the highlight layer
    styleMap = new OpenLayers.StyleMap({'strokeWidth': 5, fillOpacity: 0, strokeColor: '#ee4400', strokeOpacity: 0.5, strokeLinecap: 'round'});
    highlightLayer = new OpenLayers.Layer.Vector("Highlighted Features", {styleMap: styleMap});
    map.addLayer(highlightLayer);

    // markers layer (used for feedback)
    markers = new OpenLayers.Layer.Markers("markers");
    markers.id = "markers";
    map.addLayer(markers);
    
    map.setCenter(UNREDD.mapCenter, UNREDD.defaultZoomLevel);
    
    if (!map.getCenter()) {
        map.zoomToMaxExtent();
    }
});
