<%@ page session="true"%><%@
taglib uri="http://www.springframework.org/tags" prefix="spring"%><%@
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
page contentType="application/json" pageEncoding="UTF-8"%>"messages":{
<c:forEach var="message" items="${config.messages}" varStatus="status">  "${message.key}":"${message.value}"<c:if test="${!status.last}">,</c:if>
</c:forEach>}
