<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="net.tanesha.recaptcha.ReCaptchaImpl"%>
<%@page session="true"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="pack" uri="http://packtag.sf.net"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<c:set var="req" value="${pageContext.request}" />
<c:set var="uri" value="${req.requestURI}" />
<c:set var="base" value="${fn:replace(req.requestURL, fn:substring(uri, 0, fn:length(uri)), req.contextPath)}" />

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
    <link rel="shortcut icon" href="./myicon.ico" />   

    <script type="text/javascript">
        var languageCode = "${pageContext.response.locale}";
        var messages = <jsp:include page="messages.jsp"/>;
        var recaptchaKey = "${recaptchaKey}";
    </script>
    
    <script type="text/javascript" src="layers.json?jsonp"></script>
    
    <pack:script enabled="${config.minifiedJs}">
    	<src>/js/OpenLayers-2.12.full.js</src>
    	<!-- src>/js/OpenLayers.unredd.js</src -->
    	<src>/js/jquery-1.7.1.js</src>
    	<src>/js/jquery.mustache.js</src>
    	<src>/js/jquery-ui-1.8.16.custom.min.js</src>
    	<src>/js/jquery.fancybox.js</src>
    	<src>/js/ol-extensions/PortalToolbar.js</src>
    	<src>/js/ol-extensions/PrintToolbar.js</src>
    	<src>/js/unredd.js</src>
    	<src>/js/print-client.js</src>	
    	<src>${base}/static/custom.js</src>
    </pack:script>

    <pack:style enabled="${config.minifiedJs}">
    	<src>/css/openlayers/style.css</src>
    	<src>/css/jquery-ui-1.8.16.custom.css</src>
    	<src>/css/jquery.fancybox.css</src>
    	<src>/css/toolbar.css</src>
    	<src>${base}/static/unredd.css</src>
    </pack:style>

  </head>
  <body>
    <div id="header">
      <% if (!"off".equals(request.getParameter("header"))) { %>
        ${config.header}
      <% } %>
      <div id="toolbar">
        <c:forEach items="${config.languages}" var="lang">
          <a href="?lang=${lang.key}" class="blue_button lang_button <c:if test="${lang.key == pageContext.response.locale}">selected</c:if>" id="button_${lang.key}">${lang.value}</a>
        </c:forEach>
        <a href="#" class="blue_button" id="button_print">print</a>
        <div id="time_slider_pane">
          <div id="time_slider"></div>
          <div id="time_slider_label"></div>
        </div>
      </div>

    </div>
    
    <div id="map-tools" style="position:relative">
      <div id="layer_list_selector_pane">
        <input type="radio" id="all_layers" name="layer_list_selector" checked="checked"></input><label for="all_layers"><spring:message code="layers" /></label>
        <input type="radio" id="active_layers" name="layer_list_selector"></input><label for="active_layers"><spring:message code="selected_layers" /></label>
      </div>

      <a class="blue_button" style="z-index:1000;top:150px;margin-right:0px;position:fixed;width:60px;right:20px;margin-top:0" href="#" id="toggle_legend"><spring:message code="legend_button" /></a>

      <div style="z-index:1100;position:relative;top:40px;left:10px;width:250px;font-size:10px;">
        <div id="active_layers_pane" style="position:relative;left:0;display:none">
          <h3><a href="#">Selected Layers</a></h3>
          <div></div>
        </div>
        <div id="layers_pane" style="position:relative;top:0;left:0;display:none"></div>
      </div>

      

      <a href="#" onclick="UNREDD.map.zoomIn();return false" id="zoom_in"></a>
      <a href="#" onclick="UNREDD.map.zoomOut();return false" id="zoom_out"></a>
      <a href="#" onclick="UNREDD.map.setCenter(UNREDD.mapCenter, UNREDD.defaultZoomLevel); return false" id="zoom_to_max_extent"></a>
    </div>
    
    <% if (!"off".equals(request.getParameter("footer"))) { %>
      ${config.footer}
    <% } %>

    <div id="legend_pane" title="Legend" style="padding:2px;">
      <div id="legend_pane_content" style="background-color:#fff;width:100%;height:100%"> 
      </div>
    </div>
    
    <div id="info_popup"></div>

	<div id="invalid-mail"  style="display:none;" title="<spring:message code="invalid_email_title"/>">
		<p><spring:message code="invalid_email_text"/></p>
	</div>

  
    <!-- Print plugin elements -->
    <div id="print_popup" style="display:none;">
		<div id="print_toolbar" class="olControlPortalToolbar"></div>&nbsp;
		<div class="fb_comment"><spring:message code="feedback_text"/></div>
		<p><b>Choose the print options:</b></p>
		<br />
		<div>
			<label for="dpis"><b>dpi resolutions</b></label>
			<select id="dpis"></select>
		</div>
		<div>
			<label for="layouts"><b>Layouts</b></label>
			<select id="layouts"></select>
		</div>
		<div>
			<label for="avaialableLayers"><b>Layer</b></label>
			<select id="avaialableLayers"></select>
		</div>
		<br />
		<div>
			<input id="printSubmit" type="submit" value="Print" />
			<input id="printCancel" type="button" value="<spring:message code="cancel" />" />
		</div>
    </div>

    <div id="map"></div>
    
    <div style="display:none">
    	<div id="custom_popup"></div>
    </div>
    
  </body>
</html>
