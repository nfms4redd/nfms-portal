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

var UNREDD = {
    //wmsLayers: {},
    allLayers: {},
    visibleLayers: [],
    queryableLayers: [],
    timeDependentLayers: [],
    mapContexts: {}
};

UNREDD.wmsServers = [
    "http://unredd.geo-solutions.it",
    "http://www.rdc-snsf.org",
    "http://84.33.1.31"
]

UNREDD.Layer = function (layerName, layerDefinition)
{
    /*
    layerDefinition object example:

    "forestClassification": {
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

    this.name = layerName
    this.configuration = layerDefinition;

    // set WMS servers urls
    var baseUrl = layerDefinition.baseUrl;
    // oscarfonts: pulling 'baseurl' out of 'urls', it will render pinky patchwork if no GeoServer in localhost.
    // TODO: Move to customizable file and agree on a definite solution for this.
    var urls = []; /*baseUrl*/
    var urlsLength = UNREDD.wmsServers.length;
    for (var i = 0; i < urlsLength; i++) {
        var server = UNREDD.wmsServers[i];
        urls.push(server + baseUrl);
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
        layerName,
        urls,
        wmsParams,
        {transitionEffect: "resize", removeBackBufferDelay: 0, isBaseLayer: false, 'buffer': 0, visibility: layerDefinition.visible === 'true', projection: 'EPSG:900913', noMagic: true}
    );
}


UNREDD.Context = function (contextName, contextDefinition)
{
    /*
    contextDefinition object example:

    "administrativeUnits": {
      "infoFile": "administrative_boundaries_def.html",
      "label": "<spring:message code="admin_units" />",
      "layers": ["administrativeUnits", "administrativeUnits_simp"],
      "inlineLegendUrl": "/geoserver_drc/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=20&HEIGHT=20&LAYER=unredd:admin_units&TRANSPARENT=true"
    }
    */

    var nLayers = 0;

    this.name = contextName;
    this.configuration = contextDefinition;
    this.layers = [];

    this.setVisibility = function (active) {
        for (var i = 0; i < nLayers; i++) {
            this.layers[i].olLayer.setVisibility(active);
            this.configuration.active = active;
        }
    }

    if (contextDefinition.layers) {
        // if there's no layer property in the contextDefinition.layers, it's a stub for later use
        // mapContexts[contextName] = {};

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
        d = string.match(new RegExp(regexp)),
        offset = 0,
        date = new Date(d[1], 0, 1),
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
        markers,
        infoControl,
        getClosestPastDate,
        updateActiveLayersPane,
        mapContexts = {},
        langData,
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


    $.ajax({url: 'layers.json', dataType: 'json', async: false, success: function (data_) {
        var setupAllContexts,
            setLegends,
            loadContextGroups;

        // Create layers objects
        jQuery.each(data_.layers, function (layerName, layerDefinition) {
            var layer = new UNREDD.Layer(layerName, layerDefinition);

            if (layerDefinition.visible) {
                UNREDD.visibleLayers.push(layer.olLayer);
            }

            UNREDD.allLayers[layerName] = layer;
            if (layerDefinition.queryable) { UNREDD.queryableLayers.push(layer); }
            if (typeof layer.configuration.wmsTime !== 'undefined') { UNREDD.timeDependentLayers.push(layer) }
        });

        // Create context objects
        jQuery.each(data_.contexts, function (contextName, contextDefinition) {
            var context = new UNREDD.Context(contextName, contextDefinition);
            UNREDD.mapContexts[contextName] = context;
        });
        
        // set the langData variable (used to localized content dynamically created
        // in Javascript
        langData = data_.lang;
        
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
                        inlineLegend = $('<img class="inline-legend" src="' + contextConf.inlineLegendUrl + '">');
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
                            // TODO: localize data source link
                            table += '<td class="data_source_link"><span class="lang" id="data_source">Data source:</span> <a target="_blank" href="' + layerConf.sourceLink + '">' + layerConf.sourceLabel + '</a></td>';
                        } else {
                            table += "<td></td>";
                        }
                        table += '</tr>';
                        table += '<tr class="legend_image">';
                        table += '<td colspan="2" style="width:100%;background-color:white"><img src="loc/' + languageCode + '/images/' + layerConf.legend + '" /></td>';
                        table += '</tr>';
                        table += '</table>';
                    }

                    $('#legend_pane_content').append(table);
                }
            });
        };

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
                    active,
                    infoButton;

                if (contextGroupDefinition.hasOwnProperty('group')) {
                    // it's a group
                    if (level === 0) {
                        if (typeof contextGroupDefinition.group.infoFile !== 'undefined') {
                            accordionHeader = $('<div style="position:relative" class="accordion_header"><a style="width:190px" href="#">' + contextGroupDefinition.group.label
                                + '</a></div>');
                            infoButton = $('<a style="position:absolute;top:3px;right:7px;width:16px;height:16px;padding:0;" class="layer_info_button" href="loc/' + languageCode + '/html/' + contextGroupDefinition.group.infoFile + '"></a>')
                            accordionHeader.append(infoButton);
                            
                            // prevent accordion item from expanding when clicking on the info button
                            infoButton.click(function (event) {
                                event.stopPropagation();
                            });
                        } else {
                            accordionHeader = $("<div class=\"accordion_header\"><a href=\"#\">" +  contextGroupDefinition.group.label + "</a></div>");
                        }
                        element.append(accordionHeader);
                        contextsDiv = $("<div class=\"context_buttonset\"></div>");
                        innerElement = $('<table style="width:100%;border-collapse:collapse"></table>');

                        contextsDiv.append(innerElement);
                        element.append(contextsDiv);
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
                        active = UNREDD.mapContexts[contextName].configuration.active;

                        var context = UNREDD.mapContexts[contextName];

                        if (typeof context !== "undefined" && typeof context.configuration.layers !== "undefined") {
                            var contextConf = context.configuration

                            tr = $('<tr class="layer_row">');

                            //if (active) {
                            //  tr.addClass('active');
                            //}
                            
                            if (contextConf.hasOwnProperty('inlineLegendUrl')) {
                                // context has an inline legend
                                td1 = $('<td style="width:20px">');
                                inlineLegend = $('<img class="inline-legend" src="' + contextConf.inlineLegendUrl + '">');
                                td1.append(inlineLegend);
                            } else if (context.hasLegend) {
                                // context has a legend to be shown on the legend pane
                                // add link to show the legend pane
                                if (active) {
                                    td1 = $('<td style="font-size:9px;width:20px;height:20px"><a id="' + contextName + '_inline_legend_icon" class="inline_legend_icon on"></a></td>');
                                    // Add the legend to the legend pane (hidden when page loads)
                                    setLegends(context, true);
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
                            td3.text(contextConf.label);
                            td4        = $('<td style="width:16px;padding:0">');
                            infoButton = $('<a class="layer_info_button" id="' + contextName + '_info_button" href="loc/' + languageCode + '/html/' + contextConf.infoFile + '"></a>');
                            
                            if (typeof contextConf.infoFile !== 'undefined') {
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
                                    
                                    var active = !contextConf.active;
                                    setContextVisibility(context, active);
                                    setLegends(context, active);
                                    updateActiveLayersPane();
                                });
                            }(checkbox))
                            
                        } else if (typeof contextConf !== "undefined") {
                            tr  = $('<tr style="font-size:10px;height:22px">');
                            td1 = $('<td colspan="3" style="color:#FFF">');
                            td2 = $('<td style="width:16px;padding:0">');
                            
                            td1.text(contextConf.label);
                            
                            infoButton = $('<a class="layer_info_button" id="' + contextName + '_info_button" href="loc/' + languageCode + '/html/' + contextConf.infoFile + '"></a>');

                            td2.append(infoButton);
                            tr.append(td1, td2);
                            element.append(tr);
                        }

                        if (typeof infoButton !== 'undefined') {
                            infoButton.fancybox({
                                'autoScale' : false,
                                'openEffect' : 'elastic',
                                'closeEffect' : 'elastic',
                                'type': 'ajax',
                                'overlayOpacity': 0.5
                            });
                        }
                    }
                }
            });
        };

        loadContextGroups(contextGroups, 0, $("#layers_pane"));

        //$("#layers_pane").accordion({collapsible: true, autoHeight: false, animated: false});
        $("#layers_pane").accordion({ collapsible: true, autoHeight: false, header: ".accordion_header", animated: false } );
        $("#layers_pane").show();
        
        setupAllContexts();
    }});


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
                d.setISO8601(sDates[i]);
                dates[i] = d;
            }

            newDate = getClosestPastDate(selectedDate, dates);
            layer.olLayer.mergeNewParams({'time': isoDateString(newDate)});
        });
    };

    $("#time_slider_label").text(UNREDD.currentTime);
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
                position     = UNREDD.map.getLonLatFromPixel(e.xy);
                size         = new OpenLayers.Size(21, 25);
                offset       = new OpenLayers.Pixel(-(size.w / 2), -size.h);
                icon         = new OpenLayers.Icon('js/OpenLayers-2.11/img/marker.png', size, offset);
                markersLayer = UNREDD.map.getLayer('markers');
                marker       = new OpenLayers.Marker(position, icon);

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
    UNREDD.map.addControl(feedbackControl);
    //feedbackControl.activate();

    // Info click handler
    infoControl = new OpenLayers.Control.WMSGetFeatureInfo({
        url: 'http://localhost/geoserver/wms',
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
    
    UNREDD.map.addLayers(UNREDD.visibleLayers);
    //var wikimapia = new OpenLayers.Layer.Wikimapia( "Wikimapia",
    //  {sphericalMercator: true, isBaseLayer: false, 'buffer': 0 } );
    //map.addLayer(wikimapia);
    
    // StyleMap for the highlight layer
    styleMap = new OpenLayers.StyleMap({'strokeWidth': 5, fillOpacity: 0, strokeColor: '#ee4400', strokeOpacity: 0.5, strokeLinecap: 'round'});
    highlightLayer = new OpenLayers.Layer.Vector("Highlighted Features", {styleMap: styleMap});
    UNREDD.map.addLayer(highlightLayer);

    // markers layer (used for feedback)
    markers = new OpenLayers.Layer.Markers("markers");
    markers.id = "markers";
    UNREDD.map.addLayer(markers);
    
    UNREDD.map.setCenter(UNREDD.mapCenter, UNREDD.defaultZoomLevel);
    
    if (!UNREDD.map.getCenter()) {
        UNREDD.map.zoomToMaxExtent();
    }
});
