package org.fao.unredd.portal.stats;

import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;

public class WPSResult {
	
	UNREDDStatsDef statsDef;
	UNREDDLayerUpdate layerUpdate;
	double[][] statsData;
	
	public WPSResult(UNREDDStatsDef statsDef, UNREDDLayerUpdate layerUpdate) {
		this.statsDef = statsDef;
		this.layerUpdate = layerUpdate;
	}

	public double[][] getStatsData() {
		return statsData;
	}
	
	public void setStatsData(double[][] statsData) {
		this.statsData = statsData;
	}

}
