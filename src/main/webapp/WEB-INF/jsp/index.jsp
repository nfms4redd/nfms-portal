<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="net.tanesha.recaptcha.ReCaptchaImpl"%>
<%@page session="true"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--
 nfms4redd Portal Interface - http://nfms4redd.org/

 (C) 2012, FAO Forestry Department (http://www.fao.org/forestry/)

 This application is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation;
 version 3.0 of the License.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
-->
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    
    <title><spring:message code="title" /></title>
    
    <link type="text/css" href="css/custom-theme2/jquery-ui-1.8.16.custom.css" rel="stylesheet">
    <link rel="stylesheet" href="js/fancybox-2.0.5/source/jquery.fancybox.css" type="text/css" media="screen" />
    <!--link rel="stylesheet" href="js/OpenLayers-2.11/theme/default/style.css" type="text/css" /-->
    <link rel="stylesheet" href="js/OpenLayers-2.12/theme/default/style.css" type="text/css" />
    <link rel="stylesheet" href="css/toolbar.css" type="text/css" />
    <link rel="stylesheet" href="static/unredd.css" type="text/css">
    
    <script type="text/javascript">
        var languageCode = "${pageContext.response.locale}";
        var messages = <jsp:include page="messages.jsp"/>;
    </script>
    <script type="text/javascript" src="js/jquery-1.7.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.mustache.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
    <script type="text/javascript" src="js/fancybox-2.0.5/source/jquery.fancybox.pack.js"></script>
    <!--script type="text/javascript" src="js/OpenLayers-2.11/OpenLayers.js"></script-->
    <!-- script type="text/javascript" src="js/OpenLayers-2.12/OpenLayers.debug.js"></script-->
    <script type="text/javascript" src="js/OpenLayers-2.12/OpenLayers.js"></script>
    <script type='text/javascript' src='js/toolbar.js'></script>
    <script type='text/javascript' src='js/unredd.js'></script>
    <script type='text/javascript' src='static/custom.js'></script>
  </head>
  <body>
    <div id="header">
      ${config.header}
      
      <div id="toolbar">
        <c:forEach items="${config.languages}" var="lang">
          <a href="?lang=${lang.key}" class="blue_button lang_button <c:if test="${lang.key == pageContext.response.locale}">selected</c:if>" id="button_${lang.key}">${lang.value}</a>
        </c:forEach>
        <a href="#" class="blue_button" id="button_feedback"><spring:message code="feedback" /></a>
        <a href="#" class="blue_button" id="button_statistics"><spring:message code="statistics" /></a>
        <div id="time_slider_pane">
          <div id="time_slider"></div>
          <div id="time_slider_label"></div>
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
    
    ${config.footer}
    
    <a href="#" onclick="UNREDD.map.zoomIn();return false" id="zoom_in"></a>
    <a href="#" onclick="UNREDD.map.zoomOut();return false" id="zoom_out"></a>
    <a href="#" onclick="UNREDD.map.setCenter(UNREDD.mapCenter, UNREDD.defaultZoomLevel); return false" id="zoom_to_max_extent"></a>
    
    <div id="legend_pane" title="Legend" style="padding:2px;">
      <div id="legend_pane_content" style="background-color:#fff;width:100%;height:100%"> 
      </div>
    </div>
    
    <div id="info_popup"></div>
	<div id="feedback-invalid-mail"  style="display:none;" title="<spring:message code="invalid_email_title"/>">
		<p><spring:message code="invalid_email_text"/></p>
	</div>
    <div id="feedback_popup" style="display:none;">
      <table class="feedback">
        <tr>
          <th>
            <spring:message code="layer" />:
          </th>
          <td>
            <select id="fb_layers"></select>
            <span id="fb_time"></span>
          </td>
        </tr>
        <tr>
          <th>
            <spring:message code="feedback_drawing_tools" />:
          </th>
          <td>
             <div id="fb_toolbar" class="olControlPortalToolbar"></div>
             <div class="fb_comment"><spring:message code="feedback_text"/></div>
          </td>
        </tr>
        <tr>
          <th>
            <spring:message code="name" />:
          </th>
          <td>
            <input name="name_" id="name_" type="text">
          </td>
        </tr>
        <tr>
          <th>
            <spring:message code="email" />:
          </th>
          <td>
            <input name="email_" id="email_" type="text">
          </td>
        </tr>
        <tr>
          <th>
            <spring:message code="feedback" />:
          </th>
          <td>
            <textarea id="feedback_" name="feedback_text_"></textarea>
          </td>
        </tr>
		<tr>
          <td colspan="2" class="recaptcha">
            ${captchaHtml}
          </td>
        </tr>
        <tr>
          <td></td>
          <td colspan="2">
            <input id="feedback_submit" type="submit" value="<spring:message code="submit" />" />
            <input type="button" id="feedback_cancel" value="<spring:message code="cancel" />" />
          </td>
        </tr>
      </table>
    </div>
    <div id="feedback_info_div" style="z-index:2000;display:none;filter:alpha(opacity=75);opacity: 0.75;padding:6px 10px;position:absolute;top:150px;left:400px;background-color:black;color:#fff;"><spring:message code="feedback_info" /></div>
    <div id="statistics_info_div" style="z-index:2000;display:none;filter:alpha(opacity=75);opacity: 0.75;padding:6px 10px;position:absolute;top:150px;left:400px;background-color:black;color:#fff;"><spring:message code="statistics_info" /></div>
    
    <div id="map"></div>
    
    <div style="display:none">
    	<div id="custom_popup"></div>
    </div>
    
  </body>
</html>

