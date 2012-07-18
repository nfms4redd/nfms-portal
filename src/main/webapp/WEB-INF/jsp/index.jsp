<%@page import="java.util.Enumeration"%><%@
page import="java.util.Locale"%><%@
page import="java.util.ResourceBundle"%><%@ page session="true"
%><%@taglib uri="http://www.springframework.org/tags" prefix="spring"
%><%@
page contentType="text/html" pageEncoding="UTF-8"
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    
    <title><spring:message code="title" /></title>
    
    <link type="text/css" href="css/custom-theme2/jquery-ui-1.8.16.custom.css" rel="stylesheet">
    <link rel="stylesheet" href="js/fancybox-2.0.5/source/jquery.fancybox.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="js/OpenLayers-2.11/theme/default/style.css" type="text/css" />
    <link rel="stylesheet" href="css/unredd.css" type="text/css">
    
    <script type="text/javascript">
        var languageCode = "${pageContext.response.locale}";
    </script>
    <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
    <script type="text/javascript" src="js/fancybox-2.0.5/source/jquery.fancybox.pack.js"></script>
    <script type="text/javascript" src="js/OpenLayers-2.11/OpenLayers.js"></script>
    <script type='text/javascript' src='js/unredd.js?v=2'></script>
    <script type='text/javascript' src='js/custom.js'></script>
  </head>
  <body>
    <div id="header">
      <%@ include file="banner.jsp" %>
      
      <div id="toolbar">
        <a href="./?lang=en" class="blue_button lang_button <%= "en".equals(pageContext.getResponse().getLocale().toString()) ? "selected" : "" %>" id="button_en" style="right:10px">English</a>
        <!--<a href="index.htm?lang=es" class="blue_button lang_button <%= "es".equals(pageContext.getResponse().getLocale().toString()) ? "selected" : "" %>" id="button_es" style="right:80px">Français</a>-->
        <a href="./?lang=fr" class="blue_button lang_button <%= "fr".equals(pageContext.getResponse().getLocale().toString()) ? "selected" : "" %>" id="button_fr" style="right:80px">Français</a>
        <a href="#" class="blue_button" id="button_feedback" style="width:80px"><spring:message code="feedback" /></a>
        <a href="#" class="blue_button" id="button_statistics" style="width:80px"><spring:message code="statistics" /></a>
        <div id="time_slider_pane">
          <div id="time_slider"></div>
          <div id="time_slider_label">2005</div>
        </div>
      </div>
    </div>
    
    <div id="layer_list_selector_pane">
		  <input type="radio" id="all_layers" name="layer_list_selector" checked="checked"></input><label for="all_layers"><spring:message code="layers" /></label>
		  <input type="radio" id="active_layers" name="layer_list_selector"></input><label for="active_layers"><spring:message code="selected_layers" /></label>
    </div>
    <div style="z-index:1100;position:absolute;top:215px;left:10px;width:250px;font-size:10px;">
      <div id="active_layers_pane" style="position:relative;top:0;left:0;display:none">
        <h3><a href="#">Selected Layers</a></h3>
        <div></div>
      </div>
      <div id="layers_pane" style="position:relative;top:0;left:0;display:none"></div>   
    </div>
    
    <a class="blue_button" style="z-index:1000;top:150px;right:20px;margin-right:0px;position:absolute;width:60px" href="#" id="toggle_legend"><spring:message code="legend_button" /></a>
    
    <%@ include file="footer.jsp" %>
    
    <a href="#" onclick="UNREDD.map.zoomIn();return false" id="zoom_in"></a>
    <a href="#" onclick="UNREDD.map.zoomOut();return false" id="zoom_out"></a>
    <a href="#" onclick="UNREDD.map.setCenter(UNREDD.mapCenter, UNREDD.defaultZoomLevel); return false" id="zoom_to_max_extent"></a>
    
    <div id="legend_pane" title="Legend" style="padding:2px;">
      <div id="legend_pane_content" style="background-color:#fff;width:100%;height:100%"> 
      </div>
    </div>
    
    <div id="info_popup"></div>
    <div id="feedback_popup" style="display:none;">
      <table style="font-size:12px;color:white">
        <tr>
          <td></td>
          <td>
            Lat: <span id="fb_coord_x"></span> - Lon: <span id="fb_coord_y"></span>
          </td>
        </tr>
        <tr>
          <td>
            <b><spring:message code="layer" />:</b>
          </td>
          <td>
            <select>
              <option value="deforestation">Deforestation</option>
            </select>
          </td>
        </tr>
        <tr>
          <td>
            <b><spring:message code="year" />:</b>
          </td>
          <td>
            <select>
              <option value="2005">2000-2005</option>
              <option value="2010">2005-2010</option>
            </select>
          </td>
        </tr>
        <tr>
          <td>
            <span style="font-size:12px;color:white;font-weight:bold"><spring:message code="name" />:</span>
          </td>
          <td>
            <input name="name_" id="name_" type="text" size="28">
          </td>
        </tr>
        <tr>
          <td>
            <span style="font-size:12px;color:white;font-weight:bold"><spring:message code="email" /></span>
          </td>
          <td>
            <input name="email_" id="email_" type="text" size="28">
          </td>
        </tr>
        <tr>
          <td style="vertical-align: top">
            <b><spring:message code="feedback" />:</b>
          </td>
          <td style="vertical-align: top">
            <textarea id="feedback_" name="feedback_text_" rows="5" cols="28"></textarea>
          </td>
        </tr>
        <tr>
          <td></td>
          <td colspan="2">
            <input type="button" id="feedback_cancel" value="Cancel" />
            <input id="feedback_submit" type="submit">
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <spring:message code="feedback_text" />
          </td>
        </tr>
      </table>
    </div>
    <div id="feedback_info_div" style="z-index:2000;display:none;filter:alpha(opacity=75);opacity: 0.75;padding:6px 10px;position:absolute;top:150px;left:400px;background-color:black;color:#fff;"><spring:message code="feedback_info" /></div>
    <div id="statistics_info_div" style="z-index:2000;display:none;filter:alpha(opacity=75);opacity: 0.75;padding:6px 10px;position:absolute;top:150px;left:400px;background-color:black;color:#fff;"><spring:message code="statistics_info" /></div>
    
    <div id="map"></div>
    
  </body>
</html>
