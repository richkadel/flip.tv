<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta name="description" content="Appeligo is a new Internet Media company. Our mission is to deliver knowledge, through software and media, in new and compelling ways." />
		<meta name="keywords" content="appeligo, flip, flip tv, tv flip, television" />
		<title>Flip.TV - Login</title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<script type="text/javascript">
		function toggleSubmitButton() {
			var button = document.getElementById("submit_button");
				button.style.color = "#999999";
		}
		</script>
		<script type="text/javascript" src="${pageContext.request.contextPath}"></script>
		<script type="text/javascript">
			if (location.href.indexOf("${pageContext.request.contextPath}/login.jsp") < 0) {			
				location.href = "${pageContext.request.contextPath}/login.jsp";
			}
		</script>
	</head>
	<body>
		<table width="100%" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td class="header">
    				<table cellspacing="0" cellpadding="0">
    					<tr>
            				<td valign="top" class="logo" cellspacing="0" cellpadding="0">
            				</td>
            	</tr>
           	</table>
       	</td>
     	</tr>
    </table>

<form method="POST" action="j_security_check">
<table valign="center">
<tr><td>Username: </td><td><input type="text" name="j_username"/></td></tr>
<tr><td>Password: </td><td><input type="password" name="j_password"/></td></tr>
<tr><td>&nbsp;</td><td><input id="submit_button" type="submit" name="login" value="Login" /></td></tr>
</table>
</form>
</body>
</html>
