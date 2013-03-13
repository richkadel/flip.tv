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
sb.append("/home/home.action");
response.sendRedirect(sb.toString());
%>