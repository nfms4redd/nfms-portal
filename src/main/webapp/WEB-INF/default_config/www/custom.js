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
    /*
    pry_admin1: function (feature) {
        var that = {};
        that.title = function () { return jQuery.i18n.prop('province') + ": " + feature.attributes.NAME1; };
        that.statsLink = function () { return 'data/charts/' + languageCode + '/admin1/admin1_' + feature.attributes.ID + '.html?name=' + feature.attributes.NAME1; };
        return that;
    },

    pry_admin2: function (feature) {
        var that = {};
        that.title = function () { return jQuery.i18n.prop('admin_unit') + ": " + feature.attributes.NAME2; };
        that.statsLink = function () { return 'data/charts/' + languageCode + '/admin2/admin2_' + feature.attributes.ID + '.html?name=' + feature.attributes.NAME2; };
        return that;
    },
    */
	
    pry_landsat_p228r75: function (feature) {
        var that = {};
        that.title = function () {
            return "Datos producidos durante el taller de formaciÃ³n";
        };
        that.statsLink = function () {
            return 'data/charts/' + languageCode + '/training_data/training_chart.html';
        };
        return that;
    },
	
    pry_alto_paraguay_simp: function (feature) {
        var that = {};
        that.title = function () {
            return "Uso Agropecuario 1997 - 2001 Alto Paraguay";
        };
        that.statsLink = function () {
            return 'data/charts/' + languageCode + '/land_use/land_use.html';
        };
        return that;
    }
    
    /*
    pry_protected_areas: function (feature) {
        var that = {};
        that.title = function () {return jQuery.i18n.prop('province') + ": " + "";};
        that.statsLink = function () {return 'data/charts/' + languageCode + '/admin1/admin1_' + feature.attributes.OBJECTID + '.html?name=' + "";};
        return that;
    }
    */
};

