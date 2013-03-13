<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="com.caucho.hessian.client.HessianProxyFactory" %>
<%@ page import="com.knowbout.epg.service.EPGProvider" %>
<%@ page import="com.knowbout.epg.service.ServiceLineup" %>
<%@ page import="com.knowbout.epg.service.ScheduledProgram" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>EPG</title>
</head>
<body>
<%
//PENDING(CE): Yes this is bad and ugly code but it is just to demonstrate it 
//is working
HessianProxyFactory factory = new HessianProxyFactory();
String path = request.getServletPath();
StringBuffer url = request.getRequestURL();
int index = url.lastIndexOf(path);
String serviceUrl = url.toString();
if (index > -1) {
    serviceUrl = url.substring(0,index);
}
serviceUrl += "/channel.epg";

System.err.println("\n\nREQUEST:" +serviceUrl+"\n\n");

EPGProvider epg = (EPGProvider) factory.create(EPGProvider.class,serviceUrl);
List<ServiceLineup> list = epg.getServiceLineup("92128");
pageContext.setAttribute("lineups", list);

List channels = epg.getChannels("CA04542:R");

Date time = new Date();
List<ScheduledProgram> programs = epg.getAllScheduledPrograms("CA04542:R", time);
pageContext.setAttribute("currentTime", time);
pageContext.setAttribute("nowPlaying", programs);

%>

<h1>Service Linups for 92128</h1>
<table border="1"><tr><th>Id</th><th>Name</th><th>DMA</th><th>Lineup</th></tr>
<c:forEach var="lineup" items="${lineups}">
<tr><td>${lineup.id}</td><td>${lineup.name}</td><td>${lineup.demographicMarketArea}</td><td>${lineup.lineup}</td></tr>
</c:forEach>
</table>
<h2>Current time is ${currentTime}</h2>
<table border="1"><tr><th>Channel Number</th><th>Station</th><th>Program</th><th>Description</th></tr>
<c:forEach var="program" items="${nowPlaying}">
<tr><th>${program.channel.channel}</th><th>${program.channel.stationCallSign}</th><th>${program.programTitle}</th><th>${program.description}</th></tr>
</c:forEach>
</table>
</body>
</html>
