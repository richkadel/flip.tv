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
sb.append("/search/program.action?programId="+request.getQueryString()+"#reminders");
response.sendRedirect(sb.toString());
%>