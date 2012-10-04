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
package org.fao.unredd.portal;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.unredd.geostore.UNREDDGeostoreManager;
import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
//import org.fao.unredd.Util;

/**
 *
 * @author sgiaccio
 */
public class Layers extends HttpServlet {
	
	private static Logger logger = Logger.getLogger(Layers.class);

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    /*
        UNREDDGeostoreManager manager = new UNREDDGeostoreManager(Util.getGeostoreClient(getServletContext()));
        
        try {
            List<Resource> layers = manager.getLayers();
            for (Resource layer : layers) {
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
                    wmsTimes.append("-01T00:00:00.000Z"); // period is year or month, so the rest of the time string is always the same
                    
                    if (iterator.hasNext()) {
                        wmsTimes.append(",");
                    }
                }
                //System.out.println("layer.getName() = " + layer.getName()); // DEBUG
                request.setAttribute(layer.getName(), wmsTimes.toString());
            }
            RequestDispatcher rd = request.getRequestDispatcher("layers.jsp");
            rd.forward(request, response);
        } catch (UnsupportedEncodingException ex) {
        	logger.error(null, ex);
        } catch (JAXBException ex) {
        	logger.error(null, ex);
        }
        * */
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
