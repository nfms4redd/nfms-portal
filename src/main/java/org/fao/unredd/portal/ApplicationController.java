package org.fao.unredd.portal;

import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.services.rest.GeoStoreClient;
import it.geosolutions.unredd.geostore.UNREDDGeostoreManager;
import it.geosolutions.unredd.geostore.model.UNREDDLayer;
import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import it.geosolutions.unredd.geostore.model.UNREDDStatsDef;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ApplicationController {
    
    @Autowired
    GeoStoreClient client;
    
    @RequestMapping(value="/index.do", method=RequestMethod.GET)
    public ModelAndView index(Model model) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        
        return mv;
    }
    
    @RequestMapping(value="/layers.json", method=RequestMethod.GET)
    public ModelAndView layers(Model model) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("layers");
        
        List<Resource> layers = null;
        UNREDDGeostoreManager manager = null;
        try {
            manager = new UNREDDGeostoreManager(client);
            
            layers = manager.getLayers();
        } catch (Exception ex) {
            Logger.getLogger(Layers.class.getName()).log(Level.SEVERE, "Error connecting to GeoStore");
        }
        
        if (layers != null)
        {
            for (Resource layer : layers) {
                String wmsTimes;
                try {
                    wmsTimes = getWmsTimeString(manager, layer);
                    model.addAttribute(layer.getName(), wmsTimes.toString());
                } catch (Exception ex) {
                    Logger.getLogger(Layers.class.getName()).log(Level.SEVERE, "Error getting time dimension for layer {0}", layer.getName());
                }
            }
        }
        
        return mv;
    }

    private String getWmsTimeString(UNREDDGeostoreManager manager, Resource layer) throws JAXBException, UnsupportedEncodingException {
        StringBuilder wmsTimes = new StringBuilder();
        List<Resource> layerUpdates = manager.searchLayerUpdatesByLayerName(layer.getName());
        Iterator<Resource> iterator = layerUpdates.iterator();
        while (iterator.hasNext()) {
            UNREDDLayerUpdate unreddLayerUpdate = new UNREDDLayerUpdate(iterator.next());
            String year  = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.YEAR);
            String month = unreddLayerUpdate.getAttribute(UNREDDLayerUpdate.Attributes.MONTH);
            
            // build wms time string manually
            wmsTimes.append(year).append("-");
            
            if (month != null) {
                if (month.length() == 1) wmsTimes.append("0");
                wmsTimes.append(month);
            }
            else wmsTimes.append("01"); // Assign january if data is updated yearly and month is not there

            wmsTimes.append("-01T00:00:00.000Z"); // period is year or month, so the rest of the time string is always the same
            
            if (iterator.hasNext()) {
                wmsTimes.append(",");
            }
        }
        
        return wmsTimes.toString();
    }
    
    /*
    public static void main(String[] args) {
        LayersController controller = new LayersController();
        try {
            controller.test();
        } catch (Exception ex) {
            Logger.getLogger(LayersController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void test() throws Exception {
        UNREDDGeostoreManager manager = new UNREDDGeostoreManager("http://localhost:9191/geostore/rest", "admin", "admin");
        getStatsJson(manager);
    }
    */
    
    private String getStatsJson(UNREDDGeostoreManager manager) throws Exception
    {
        JSONObject jsonRoot = new JSONObject();
        
        List<Resource> statsDefResources = manager.getStatsDefs();
        for (Resource statsDefResource : statsDefResources)
        {
            JSONObject statsDefJsonObj = new JSONObject();
            
            UNREDDStatsDef unreddStatsDef = new UNREDDStatsDef(statsDefResource);
            List<String> statsDefLayerNames = unreddStatsDef.getReverseAttributes(UNREDDStatsDef.ReverseAttributes.LAYER.getName());
            
            String zonalLayerName = unreddStatsDef.getAttribute(UNREDDStatsDef.Attributes.ZONALLAYER);
            
            JSONObject layersJsonObj = new JSONObject();
            String zonalLayerAttributeId = "no_zonal_attribute_found"; // DEBUG
            for (String layerName : statsDefLayerNames) {
                Resource layerResource = manager.searchLayer(layerName); // TODO: optimize this
                UNREDDLayer unreddLayer = new UNREDDLayer(layerResource);
                boolean isZonalLayer = zonalLayerName.equals(layerName);
                JSONObject layerJsonObj = getLayerJsonObj(layerName, isZonalLayer);
                if (isZonalLayer) zonalLayerAttributeId = unreddLayer.getAttribute(UNREDDLayer.Attributes.RASTERATTRIBNAME);
                layersJsonObj.element(layerName, layerJsonObj);
            }
            
            statsDefJsonObj.element("label", "this is the statsDef label");
            statsDefJsonObj.element("layers", layersJsonObj);
            
            // build stats url
            StringBuilder urlStringBuilder = new StringBuilder("/misc/category/ChartData/resource/");
            urlStringBuilder.append(statsDefResource.getName());
            urlStringBuilder.append("_%");
            urlStringBuilder.append(zonalLayerAttributeId);
            urlStringBuilder.append("%");
            
            statsDefJsonObj.element("url", urlStringBuilder.toString());
           
            jsonRoot.element(statsDefResource.getName(), statsDefJsonObj);
      }
        
        System.out.println(jsonRoot.toString()); // DEBUG
        return jsonRoot.toString();
    }

    private JSONObject getLayerJsonObj(String layerName, boolean isZonalLayer) throws Exception {
        JSONObject jsonObj = new JSONObject();
        jsonObj.element("wmsName", layerName);
        if (isZonalLayer) {
            jsonObj.element("zonal", isZonalLayer);
        }
                
        return jsonObj;
    }
    
}
