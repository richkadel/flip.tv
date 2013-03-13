<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Extract Search Words</title>
</head>
<body>
	<form action="searchwords.extract" method="post">
		<table>
			<tr><td>
			<textarea cols="80" rows="25" name="text">Enter text here.</textarea>
			</td></tr>
			<tr><td>
			<input type="checkbox" name="details" value="true" CHECKED/> full details
			<input type="submit" value="Submit"/>
			</td></tr>
		</table>
	</form>
</body>
</html>
