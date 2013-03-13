<% 
String context = request.getContextPath();
StringBuilder sb = new StringBuilder();
if (context != null && context.trim().length() > 0) {
	context = context.trim();
	if (!context.startsWith("/")) {
		context = "/" + context;
	}
	sb.append(context);
}
sb.append("/alerts/alerts.action#"+request.getQueryString());
response.sendRedirect(sb.toString());
%>