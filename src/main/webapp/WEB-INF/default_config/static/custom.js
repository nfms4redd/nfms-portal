UNREDD.maxExtent = new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508);
UNREDD.restrictedExtent = new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508);
UNREDD.maxResolution = 4891.969809375;
UNREDD.mapCenter = new OpenLayers.LonLat(2500000, -400000);
UNREDD.defaultZoomLevel = 0;

UNREDD.minTime     = 2000;
UNREDD.maxTime     = 2005;
UNREDD.currentTime = 2005;
UNREDD.timeStep    = 5;

UNREDD.layerInfo = {
    provinces: function(feature) {
       var that = {};
       that.title = function() {
           return "Province: " + feature.attributes.PROVINCE;
       };
       that.statsLink = function() {
           return '/stg_geostore/rest/misc/category/name/ChartData/resource/name/deforestation_script_' +  feature.attributes.OBJECTID + '_' + languageCode + '/data?name=' + feature.attributes.PROVINCE;
       };

       return that;
    }    
};

